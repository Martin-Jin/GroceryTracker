package com.martin.storage.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalFoodItem(
    val name: String,           // lowercase key used for lookup
    val displayName: String,    // properly-cased display name
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val defaultUnit: String = "g",
    val defaultAmount: Double = 100.0,
    val lowStockThreshold: Double = 1.0,
    val nutrition: NutritionInfo = NutritionInfo()
)

/** Small built-in seed dataset — extended at runtime by user additions/edits. */
val builtInFoodItems: List<LocalFoodItem> = listOf(
    LocalFoodItem("chicken breast", "Chicken Breast", "Meat & Seafood",
        listOf("Protein", "Meat"), "g", 500.0, 200.0,
        NutritionInfo(165.0, 31.0, 0.0, 3.6, 0.0, 0.0, 0.0, 1.04, 15.0, 0.1, 9.0, 29.0, 1.02)),
    LocalFoodItem("spinach", "Spinach", "Produce",
        listOf("Vegetable", "Iron", "Vitamin C"), "g", 200.0, 50.0,
        NutritionInfo(23.0, 2.9, 3.6, 0.4, 2.2, 0.4, 28.1, 2.71, 99.0, 0.0, 469.0, 79.0, 0.53)),
    LocalFoodItem("milk", "Milk", "Dairy",
        listOf("Dairy", "Calcium"), "l", 2.0, 0.5,
        NutritionInfo(42.0, 3.4, 5.0, 1.0, 0.0, 5.0, 0.2, 0.03, 113.0, 0.1, 46.0, 10.0, 0.37)),
    LocalFoodItem("oats", "Oats", "Pantry",
        listOf("Fibre", "Grains"), "g", 500.0, 100.0,
        NutritionInfo(389.0, 16.9, 66.3, 6.9, 10.6, 0.0, 0.0, 4.72, 54.0, 0.0, 0.0, 177.0, 3.97)),
    LocalFoodItem("salmon", "Salmon", "Meat & Seafood",
        listOf("Omega-3", "Protein"), "g", 300.0, 100.0,
        NutritionInfo(208.0, 20.1, 0.0, 13.4, 0.0, 0.0, 0.0, 0.80, 9.0, 11.1, 58.0, 27.0, 0.36)),
    LocalFoodItem("greek yogurt", "Greek Yogurt", "Dairy",
        listOf("Probiotics", "Protein"), "g", 500.0, 150.0,
        NutritionInfo(59.0, 9.9, 3.6, 0.4, 0.0, 3.2, 0.5, 0.10, 110.0, 0.0, 11.0, 11.0, 0.52)),
    LocalFoodItem("banana", "Banana", "Produce",
        listOf("Fruit"), "units", 6.0, 2.0,
        NutritionInfo(89.0, 1.1, 23.0, 0.3, 2.6, 12.2, 8.7, 0.26, 5.0, 0.0, 3.0, 27.0, 0.15)),
    LocalFoodItem("egg", "Eggs", "Dairy",
        listOf("Protein"), "units", 12.0, 4.0,
        NutritionInfo(155.0, 12.6, 1.1, 10.6, 0.0, 1.1, 0.0, 1.83, 56.0, 2.0, 149.0, 12.0, 1.29)),
    LocalFoodItem("broccoli", "Broccoli", "Produce",
        listOf("Vegetable", "Vitamin C", "Fibre"), "g", 300.0, 100.0,
        NutritionInfo(34.0, 2.8, 7.0, 0.4, 2.6, 1.7, 89.2, 0.73, 47.0, 0.0, 31.0, 21.0, 0.41)),
    LocalFoodItem("rice", "Rice", "Pantry",
        listOf("Grains", "Carbs"), "g", 1000.0, 200.0,
        NutritionInfo(130.0, 2.7, 28.2, 0.3, 0.4, 0.0, 0.0, 0.20, 10.0, 0.0, 0.0, 12.0, 0.49)),
    LocalFoodItem("avocado", "Avocado", "Produce",
        listOf("Healthy Fat", "Fruit"), "units", 4.0, 1.0,
        NutritionInfo(160.0, 2.0, 8.5, 14.7, 6.7, 0.7, 10.0, 0.61, 12.0, 0.0, 7.0, 29.0, 0.64)),
    LocalFoodItem("almonds", "Almonds", "Snacks",
        listOf("Nuts", "Healthy Fat"), "g", 200.0, 50.0,
        NutritionInfo(579.0, 21.2, 21.6, 49.9, 12.5, 3.9, 0.0, 3.71, 264.0, 0.0, 0.0, 270.0, 3.12)),
    LocalFoodItem("olive oil", "Olive Oil", "Pantry",
        listOf("Healthy Fat"), "ml", 500.0, 100.0,
        NutritionInfo(884.0, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, 0.56, 1.0, 0.0, 0.0, 0.0, 0.0)),
    LocalFoodItem("garlic", "Garlic", "Produce",
        listOf("Vegetable", "Spice"), "units", 3.0, 1.0,
        NutritionInfo(149.0, 6.4, 33.1, 0.5, 2.1, 1.0, 31.2, 1.70, 181.0, 0.0, 0.0, 25.0, 1.16)),
    LocalFoodItem("tomato", "Tomato", "Produce",
        listOf("Vegetable", "Vitamin C"), "units", 6.0, 2.0,
        NutritionInfo(18.0, 0.9, 3.9, 0.2, 1.2, 2.6, 13.7, 0.27, 10.0, 0.0, 42.0, 11.0, 0.17)),
)