package com.martin.storage.ui.meals

import androidx.lifecycle.*
import com.martin.storage.data.model.*
import com.martin.storage.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MealsUiState(
    val recipes: List<Recipe>              = emptyList(),
    val mealPlan: List<MealPlanEntry>      = emptyList(),
    val preparedMeals: List<PreparedMeal>  = emptyList(),
    val inventoryItems: List<GroceryItem>  = emptyList(),
    val isLoading: Boolean                 = true,
    val isGenerating: Boolean              = false,
    val searchQuery: String                = "",
    val selectedMealType: String?          = null,
    val appliedTagFilters: Set<String>     = emptySet(),
    val canUndo: Boolean                   = false,
    val canRedo: Boolean                   = false
) {
    val allRecipeTags: List<String>
        get() = recipes.flatMap { it.tags }.distinct().sorted()

    val filteredRecipes: List<Recipe>
        get() {
            var list = recipes
            if (searchQuery.isNotBlank()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (selectedMealType != null) list = list.filter { it.mealTypes.contains(selectedMealType) }
            if (appliedTagFilters.isNotEmpty()) list = list.filter { recipe ->
                appliedTagFilters.all { filterTag ->
                    recipe.tags.any { it.equals(filterTag, ignoreCase = true) }
                }
            }
            return list.sortedBy { it.name }
        }

    val weekPlanByDay: Map<String, List<MealPlanEntry>>
        get() = mealPlan.groupBy { it.date }.toSortedMap(compareBy {
            try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it)?.time ?: 0L } catch (_: Exception) { 0L }
        })

    val recentPrepared: List<PreparedMeal>
        get() = preparedMeals.sortedByDescending { it.date }.take(30)
}

class MealsViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(MealsUiState())
    val state: StateFlow<MealsUiState> = _state.asStateFlow()

    private val undoRedoRecipes  = UndoRedoManager<List<Recipe>>()
    private val undoRedoPlan     = UndoRedoManager<List<MealPlanEntry>>()

    init {
        viewModelScope.launch {
            combine(
                repo.recipes,
                repo.mealPlan,
                repo.preparedMeals,
                repo.groceryItems
            ) { recipes, plan, prepared, items ->
                _state.update { it.copy(
                    recipes       = recipes,
                    mealPlan      = plan,
                    preparedMeals = prepared,
                    inventoryItems = items,
                    isLoading     = false
                ) }
            }.collect()
        }
    }

    // ── Recipe CRUD ───────────────────────────────────────────────────────────

    fun upsertRecipe(recipe: Recipe) = viewModelScope.launch {
        undoRedoRecipes.push(_state.value.recipes)
        repo.upsertRecipe(recipe)
        _state.update { it.copy(canUndo = undoRedoRecipes.canUndo) }
    }

    fun deleteRecipe(id: String) = viewModelScope.launch {
        undoRedoRecipes.push(_state.value.recipes)
        repo.deleteRecipe(id)
        _state.update { it.copy(canUndo = undoRedoRecipes.canUndo) }
    }

    fun undoRecipes() = viewModelScope.launch {
        undoRedoRecipes.undo(_state.value.recipes)?.let { repo.saveRecipes(it) }
        _state.update { it.copy(canUndo = undoRedoRecipes.canUndo, canRedo = undoRedoRecipes.canRedo) }
    }

    // ── Meal Plan ─────────────────────────────────────────────────────────────

    fun addMealPlanEntry(entry: MealPlanEntry) = viewModelScope.launch {
        undoRedoPlan.push(_state.value.mealPlan)
        repo.saveMealPlan(_state.value.mealPlan + entry)
        _state.update { it.copy(canUndo = true) }
    }

    fun removeMealPlanEntry(id: String) = viewModelScope.launch {
        undoRedoPlan.push(_state.value.mealPlan)
        repo.removeMealPlanEntry(id)
        _state.update { it.copy(canUndo = true) }
    }

    fun undoMealPlan() = viewModelScope.launch {
        undoRedoPlan.undo(_state.value.mealPlan)?.let { repo.saveMealPlan(it) }
        _state.update { it.copy(canUndo = undoRedoPlan.canUndo, canRedo = undoRedoPlan.canRedo) }
    }

    fun generateWeeklyPlan() = viewModelScope.launch {
        _state.update { it.copy(isGenerating = true) }
        val plan = repo.generateWeeklyMealPlan()
        repo.saveMealPlan(plan)
        _state.update { it.copy(isGenerating = false) }
    }

    fun clearWeekPlan() = viewModelScope.launch {
        undoRedoPlan.push(_state.value.mealPlan)
        repo.saveMealPlan(emptyList())
    }

    // ── Prepared Meals ────────────────────────────────────────────────────────

    fun logMealAsCooked(recipe: Recipe, servings: Double) = viewModelScope.launch {
        val nutrition = recipe.nutritionPerServing * servings
        val meal = PreparedMeal(
            date = todayDateStr(),
            recipeId = recipe.id,
            recipeName = recipe.name,
            servings = servings,
            nutritionConsumed = nutrition
        )
        repo.logPreparedMeal(meal, recipe)
    }

    fun deletePreparedMeal(id: String) = viewModelScope.launch {
        repo.deletePreparedMeal(id)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun setSearch(q: String) = _state.update { it.copy(searchQuery = q) }
    fun setMealTypeFilter(type: String?) = _state.update { it.copy(selectedMealType = type) }
    fun applyTagFilter(tag: String)  = _state.update { it.copy(appliedTagFilters = it.appliedTagFilters + tag) }
    fun removeTagFilter(tag: String) = _state.update { it.copy(appliedTagFilters = it.appliedTagFilters - tag) }

    fun canMakeRecipe(recipe: Recipe): Boolean {
        val items = _state.value.inventoryItems
        return recipe.ingredients.filter { !it.optional }.all { ing ->
            items.any { it.name.equals(ing.itemName, ignoreCase = true) && it.amount >= ing.amount }
        }
    }

    fun missingIngredients(recipe: Recipe): List<RecipeIngredient> {
        val items = _state.value.inventoryItems
        return recipe.ingredients.filter { !it.optional }.filter { ing ->
            items.none { it.name.equals(ing.itemName, ignoreCase = true) && it.amount >= ing.amount }
        }
    }
}

class MealsViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = MealsViewModel(repo) as T
}