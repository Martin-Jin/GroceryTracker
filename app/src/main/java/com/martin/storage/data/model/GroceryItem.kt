package com.martin.storage.data.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class GroceryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double = 0.0,
    val unit: String = "units",
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val expiryDate: String = "",          // dd/MM/yyyy, empty = no expiry
    val lowStockThreshold: Double = 1.0,
    val addedDate: String = todayDateStr(),
    val nutrition: NutritionInfo = NutritionInfo(),
    val notes: String = "",
    val imageUri: String = "",
    val portionSize: Double = 1.0,
) {
    val isLowStock: Boolean
        get() = amount in 0.0001..lowStockThreshold

    val isOutOfStock: Boolean
        get() = amount <= 0

    val daysUntilExpiry: Int?
        get() {
            if (expiryDate.isEmpty()) return null
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val expiry = sdf.parse(expiryDate) ?: return null
                val diff = expiry.time - System.currentTimeMillis()
                (diff / 86_400_000).toInt()
            } catch (e: Exception) { null }
        }

    val isExpiringSoon: Boolean
        get() = daysUntilExpiry?.let { it in 0..3 } ?: false

    val isExpired: Boolean
        get() = daysUntilExpiry?.let { it < 0 } ?: false
}

enum class GroceryUnit(val label: String) {
    UNITS("units"), GRAMS("g"), KILOGRAMS("kg"),
    MILLILITERS("ml"), LITERS("l"), OUNCES("oz"),
    POUNDS("lbs"), CUPS("cups"), TABLESPOONS("tbsp"), TEASPOONS("tsp")
}

val defaultUnits = GroceryUnit.entries.map { it.label }

fun todayDateStr(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

val groceryCategories = listOf(
    "General", "Produce", "Dairy", "Meat & Seafood", "Bakery",
    "Pantry", "Frozen", "Beverages", "Snacks", "Condiments", "Household"
)