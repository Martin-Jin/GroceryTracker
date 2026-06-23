package com.martin.storage.data.repository

import android.content.Context
import com.martin.storage.data.model.UserSettings
import com.martin.storage.data.*
import com.martin.storage.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val context: Context) {

    // ── Grocery Items ────────────────────────────────────────────────────────

    val groceryItems: Flow<List<GroceryItem>> = context.loadList(DataKeys.GROCERY_ITEMS)

    suspend fun saveGroceryItems(items: List<GroceryItem>) =
        context.saveList(DataKeys.GROCERY_ITEMS, items)

    suspend fun upsertGroceryItem(item: GroceryItem) {
        val list = groceryItems.firstOrNull() ?: emptyList()
        val updated = if (list.any { it.id == item.id })
            list.map { if (it.id == item.id) item else it }
        else
            list + item
        saveGroceryItems(updated)
    }

    suspend fun deleteGroceryItem(id: String) {
        val list = groceryItems.firstOrNull() ?: emptyList()
        saveGroceryItems(list.filter { it.id != id })
    }

    suspend fun adjustAmount(id: String, delta: Double) {
        val list = groceryItems.firstOrNull() ?: emptyList()
        saveGroceryItems(list.map {
            if (it.id == id) it.copy(amount = maxOf(0.0, it.amount + delta)) else it
        })
    }

    // ── Recipes ──────────────────────────────────────────────────────────────

    val recipes: Flow<List<Recipe>> = context.loadList(DataKeys.RECIPES)

    suspend fun saveRecipes(recipes: List<Recipe>) =
        context.saveList(DataKeys.RECIPES, recipes)

    suspend fun upsertRecipe(recipe: Recipe) {
        val list = recipes.firstOrNull() ?: emptyList()
        val updated = if (list.any { it.id == recipe.id })
            list.map { if (it.id == recipe.id) recipe else it }
        else
            list + recipe
        saveRecipes(updated)
    }

    suspend fun deleteRecipe(id: String) {
        val list = recipes.firstOrNull() ?: emptyList()
        saveRecipes(list.filter { it.id != id })
    }

    // ── Meal Plan ────────────────────────────────────────────────────────────

    val mealPlan: Flow<List<MealPlanEntry>> = context.loadList(DataKeys.MEAL_PLAN)

    suspend fun saveMealPlan(plan: List<MealPlanEntry>) =
        context.saveList(DataKeys.MEAL_PLAN, plan)

    suspend fun removeMealPlanEntry(id: String) {
        val list = mealPlan.firstOrNull() ?: emptyList()
        saveMealPlan(list.filter { it.id != id })
    }

    // ── Prepared Meals ───────────────────────────────────────────────────────

    val preparedMeals: Flow<List<PreparedMeal>> = context.loadList(DataKeys.PREPARED_MEALS)

    /** Logs a meal as cooked and deducts ingredients from inventory. */
    suspend fun logPreparedMeal(meal: PreparedMeal, recipe: Recipe) {
        val current = preparedMeals.firstOrNull() ?: emptyList()
        context.saveList(DataKeys.PREPARED_MEALS, current + meal)

        // Deduct ingredient amounts proportionally
        val items = groceryItems.firstOrNull()?.toMutableList() ?: return
        recipe.ingredients.forEach { ingredient ->
            val idx = items.indexOfFirst {
                it.name.equals(ingredient.itemName, ignoreCase = true)
            }
            if (idx >= 0) {
                val item = items[idx]
                val deduct = ingredient.amount * (meal.servings / recipe.servings)
                items[idx] = item.copy(amount = maxOf(0.0, item.amount - deduct))
            }
        }
        saveGroceryItems(items)
    }

    suspend fun deletePreparedMeal(id: String) {
        val list = preparedMeals.firstOrNull() ?: emptyList()
        context.saveList(DataKeys.PREPARED_MEALS, list.filter { it.id != id })
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    val userSettings: Flow<UserSettings> =
        context.loadObject(DataKeys.USER_SETTINGS, UserSettings())

    suspend fun saveUserSettings(settings: UserSettings) =
        context.saveObject(DataKeys.USER_SETTINGS, settings)

    // ── Business logic ───────────────────────────────────────────────────────

    /** Items where amount ≤ threshold. */
    suspend fun getShoppingList(): List<GroceryItem> =
        (groceryItems.firstOrNull() ?: emptyList())
            .filter { it.isLowStock || it.isOutOfStock }

    /** Check if all required ingredients are available for a recipe. */
    suspend fun canMakeRecipe(recipe: Recipe): Boolean {
        val items = groceryItems.firstOrNull() ?: emptyList()
        return recipe.ingredients.filter { !it.optional }.all { ing ->
            items.any { it.name.equals(ing.itemName, ignoreCase = true) && it.amount >= ing.amount }
        }
    }

    /**
     * Generate a weekly meal plan from saved recipes.
     * Priority: nutrition completeness first, then ingredient availability.
     */
    suspend fun generateWeeklyMealPlan(): List<MealPlanEntry> {
        val allRecipes = recipes.firstOrNull() ?: return emptyList()
        val settings   = userSettings.firstOrNull() ?: UserSettings()

        // Simulate available inventory
        val simItems = (groceryItems.firstOrNull() ?: emptyList()).toMutableList()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val plan = mutableListOf<MealPlanEntry>()

        val slots = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)

        // Track accumulated nutrition for the week
        var weeklyNutrition = NutritionInfo()

        for (dayOffset in 0 until 7) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            val dateStr = sdf.format(cal.time)

            for (slot in slots) {
                // Candidates: recipes for this slot that can be made
                val candidates = allRecipes.filter { recipe ->
                    recipe.mealTypes.contains(slot.name) &&
                            canMakeWithItems(recipe, simItems)
                }

                // Score each candidate by nutrition gap fulfillment
                val best = candidates.maxByOrNull { recipe ->
                    nutritionGapScore(recipe.nutritionPerServing, weeklyNutrition, settings, (dayOffset + 1) * slots.size)
                }

                best?.let { recipe ->
                    plan += MealPlanEntry(
                        date       = dateStr,
                        mealType   = slot.name,
                        recipeId   = recipe.id,
                        recipeName = recipe.name,
                        servings   = 1
                    )
                    weeklyNutrition = weeklyNutrition + recipe.nutritionPerServing
                    deductSim(recipe, simItems, 1.0)
                }
            }
        }
        return plan
    }

    private fun canMakeWithItems(recipe: Recipe, items: List<GroceryItem>): Boolean =
        recipe.ingredients.filter { !it.optional }.all { ing ->
            items.any { it.name.equals(ing.itemName, ignoreCase = true) && it.amount >= ing.amount }
        }

    private fun deductSim(recipe: Recipe, items: MutableList<GroceryItem>, servings: Double) {
        recipe.ingredients.forEach { ing ->
            val idx = items.indexOfFirst { it.name.equals(ing.itemName, ignoreCase = true) }
            if (idx >= 0) {
                val item = items[idx]
                items[idx] = item.copy(amount = maxOf(0.0, item.amount - ing.amount * servings / recipe.servings))
            }
        }
    }

    /** Higher score = better closes the gap between consumed and targets. */
    private fun nutritionGapScore(
        nutrition: NutritionInfo,
        accumulated: NutritionInfo,
        settings: UserSettings,
        mealsElapsed: Int
    ): Double {
        val target = settings.dailyCalories * 7.0 // rough weekly target
        val gaps = listOf(
            (settings.dailyCalories * 7 - accumulated.calories) to nutrition.calories,
            (settings.dailyProtein  * 7 - accumulated.protein)  to nutrition.protein,
            (settings.dailyCarbs    * 7 - accumulated.carbs)    to nutrition.carbs,
            (settings.dailyVitaminC * 7 - accumulated.vitaminC) to nutrition.vitaminC,
            (settings.dailyIron     * 7 - accumulated.iron)     to nutrition.iron,
            (settings.dailyCalcium  * 7 - accumulated.calcium)  to nutrition.calcium
        )
        return gaps.sumOf { (gap, contribution) ->
            if (gap > 0) minOf(contribution / maxOf(gap, 0.001), 1.0) else 0.0
        }
    }

    // ── Seed sample data on first launch ─────────────────────────────────────

    suspend fun seedIfFirstLaunch() {
        val settings = userSettings.firstOrNull() ?: UserSettings()
        if (settings.firstLaunchSeeded) return
        saveGroceryItems(sampleGroceryItems)
        saveRecipes(sampleRecipes)
        saveUserSettings(settings.copy(firstLaunchSeeded = true))
    }
}