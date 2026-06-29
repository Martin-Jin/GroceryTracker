package com.martin.storage.ui.nutrition

import androidx.lifecycle.*
import com.martin.storage.data.model.*
import com.martin.storage.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NutritionUiState(
    val settings: UserSettings             = UserSettings(),
    val preparedMeals: List<PreparedMeal>  = emptyList(),
    val inventoryItems: List<GroceryItem>  = emptyList(),
    val isLoading: Boolean                 = true,
    val selectedPeriod: NutritionPeriod    = NutritionPeriod.TODAY
) {
    val todayStr: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private val mealsInPeriod: List<PreparedMeal>
        get() = when (selectedPeriod) {
            NutritionPeriod.TODAY  -> preparedMeals.filter { it.date == todayStr && it.eaten }
            NutritionPeriod.WEEK   -> preparedMeals.filter { isWithinDays(it.date, 7) && it.eaten }
            NutritionPeriod.MONTH  -> preparedMeals.filter { isWithinDays(it.date, 30) && it.eaten }
        }

    val accumulated: NutritionInfo
        get() = mealsInPeriod.fold(NutritionInfo()) { acc, meal -> acc + meal.nutritionConsumed }

    /** Target scaled to selected period */
    val periodTarget: NutritionInfo
        get() {
            val days = when (selectedPeriod) {
                NutritionPeriod.TODAY -> 1.0
                NutritionPeriod.WEEK  -> 7.0
                NutritionPeriod.MONTH -> 30.0
            }
            return with(settings) {
                NutritionInfo(
                    dailyCalories * days, dailyProtein * days, dailyCarbs * days,
                    dailyFat * days, dailyFiber * days, dailySugar * days,
                    dailyVitaminC * days, dailyIron * days, dailyCalcium * days,
                    dailyVitaminD * days, dailyVitaminA * days, dailyMagnesium * days, dailyZinc * days
                )
            }
        }

    val calorieProgress: Float
        get() = (accumulated.calories / periodTarget.calories).coerceIn(0.0, 1.5).toFloat()

    val dailyMealCount: Int
        get() = preparedMeals.count { it.date == todayStr && it.eaten }

    /** Top 3 inventory items per nutrient for suggestions */
    fun topItemsForNutrient(selector: (NutritionInfo) -> Double): List<Pair<GroceryItem, Double>> =
        inventoryItems
            .asSequence()
            .filter { it.amount > 0 }
            .map { item ->
                val per100 = selector(item.nutrition)
                val totalInStock = when (item.unit.lowercase()) {
                    "kg", "l" -> per100 * item.amount * 10 // kg→100g units
                    "g", "ml" -> per100 * (item.amount / 100.0)
                    else      -> per100 * item.amount
                }
                item to totalInStock
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
            .toList()

    private fun isWithinDays(dateStr: String, days: Int): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val d = sdf.parse(dateStr) ?: return false
            val cutoff = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.time
            d.after(cutoff)
        } catch (_: Exception) { false }
    }
}

enum class NutritionPeriod(val label: String) {
    TODAY("Today"), WEEK("7 Days"), MONTH("30 Days")
}

/** Micro-nutrient display descriptor. */
data class MicroNutrient(
    val label: String,
    val unit: String,
    val current: Double,
    val target: Double,
    val selector: (NutritionInfo) -> Double,
    val color: androidx.compose.ui.graphics.Color
)

class NutritionViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(NutritionUiState())
    val state: StateFlow<NutritionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.userSettings, repo.preparedMeals, repo.groceryItems) { settings, meals, items ->
                _state.update { it.copy(settings = settings, preparedMeals = meals, inventoryItems = items, isLoading = false) }
            }.collect()
        }
    }

    fun setPeriod(period: NutritionPeriod) = _state.update { it.copy(selectedPeriod = period) }
}

class NutritionViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = NutritionViewModel(repo) as T
}