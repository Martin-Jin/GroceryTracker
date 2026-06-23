package com.martin.storage.data.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val servings: Int = 2,
    val prepTimeMinutes: Int = 10,
    val cookTimeMinutes: Int = 20,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val mealTypes: List<String> = listOf(MealType.DINNER.name),
    val imageUri: String = "",
    /** Aggregate nutrition for ONE serving (computed at save time). */
    val nutritionPerServing: NutritionInfo = NutritionInfo()
)

@Serializable
data class RecipeIngredient(
    val itemName: String,
    val amount: Double,
    val unit: String,
    val optional: Boolean = false
)

enum class MealType(val label: String, val emoji: String) {
    BREAKFAST("Breakfast", "🌅"),
    LUNCH("Lunch", "☀️"),
    DINNER("Dinner", "🌙"),
    SNACK("Snack", "🍎")
}

// ---- Sample data used in @Preview and first-launch seeding ----

val sampleRecipes = listOf(
    Recipe(
        id = "sample_chicken_stirfry",
        name = "Chicken & Veggie Stir Fry",
        description = "A quick, high-protein stir fry with seasonal vegetables over fluffy rice.",
        servings = 2,
        prepTimeMinutes = 10,
        cookTimeMinutes = 15,
        ingredients = listOf(
            RecipeIngredient("Chicken Breast", 300.0, "g"),
            RecipeIngredient("Broccoli", 200.0, "g"),
            RecipeIngredient("Carrot", 100.0, "g"),
            RecipeIngredient("Garlic", 10.0, "g"),
            RecipeIngredient("Soy Sauce", 30.0, "ml"),
            RecipeIngredient("Rice", 200.0, "g"),
            RecipeIngredient("Olive Oil", 15.0, "ml", optional = true)
        ),
        instructions = listOf(
            "Cook rice per packet instructions.",
            "Slice chicken into strips and season with salt and pepper.",
            "Heat oil in a wok over high heat until shimmering.",
            "Cook chicken 5–6 min until golden; set aside.",
            "Stir-fry garlic 30 s, then add broccoli and carrot.",
            "Return chicken, splash in soy sauce, toss 1 min.",
            "Serve immediately over rice."
        ),
        tags = listOf("High Protein", "Quick", "Asian", "Gluten-Free Option"),
        mealTypes = listOf(MealType.LUNCH.name, MealType.DINNER.name),
        nutritionPerServing = NutritionInfo(480.0, 44.0, 55.0, 8.5, 5.0, 4.0, 48.0, 3.2, 88.0, 0.5, 520.0, 62.0, 2.7)
    ),
    Recipe(
        id = "sample_overnight_oats",
        name = "Overnight Oats",
        description = "Creamy, high-fibre oats you prep the night before—no cooking required.",
        servings = 1,
        prepTimeMinutes = 5,
        cookTimeMinutes = 0,
        ingredients = listOf(
            RecipeIngredient("Oats", 80.0, "g"),
            RecipeIngredient("Milk", 200.0, "ml"),
            RecipeIngredient("Greek Yogurt", 100.0, "g"),
            RecipeIngredient("Banana", 60.0, "g"),
            RecipeIngredient("Blueberries", 40.0, "g", optional = true)
        ),
        instructions = listOf(
            "Combine oats and milk in a jar or container.",
            "Stir in yogurt and mix well.",
            "Seal and refrigerate overnight (at least 6 h).",
            "Top with sliced banana and blueberries before serving."
        ),
        tags = listOf("Breakfast", "Meal Prep", "High Fibre", "No-Cook"),
        mealTypes = listOf(MealType.BREAKFAST.name),
        nutritionPerServing = NutritionInfo(390.0, 19.0, 66.0, 7.2, 8.5, 22.0, 12.0, 2.4, 260.0, 0.6, 54.0, 52.0, 2.1)
    ),
    Recipe(
        id = "sample_salmon_bowl",
        name = "Zesty Salmon Power Bowl",
        description = "Omega-3-packed salmon with avocado, quinoa, and lemon dressing.",
        servings = 2,
        prepTimeMinutes = 10,
        cookTimeMinutes = 15,
        ingredients = listOf(
            RecipeIngredient("Salmon", 300.0, "g"),
            RecipeIngredient("Avocado", 150.0, "g"),
            RecipeIngredient("Spinach", 100.0, "g"),
            RecipeIngredient("Lemon", 30.0, "ml"),
            RecipeIngredient("Olive Oil", 20.0, "ml"),
            RecipeIngredient("Garlic", 5.0, "g")
        ),
        instructions = listOf(
            "Season salmon fillets with salt, pepper, and garlic.",
            "Pan-sear in olive oil 4–5 min each side until cooked through.",
            "Toss spinach with lemon juice; slice avocado.",
            "Plate spinach, top with salmon and avocado. Drizzle remaining lemon oil."
        ),
        tags = listOf("Omega-3", "Low Carb", "High Protein", "Keto-Friendly"),
        mealTypes = listOf(MealType.LUNCH.name, MealType.DINNER.name),
        nutritionPerServing = NutritionInfo(420.0, 34.0, 8.0, 28.0, 6.0, 1.5, 22.0, 2.5, 55.0, 11.2, 270.0, 55.0, 1.8)
    )
)

val sampleGroceryItems = listOf(
    GroceryItem(id = "demo_1", name = "Chicken Breast", amount = 500.0, unit = "g", category = "Meat & Seafood",
        tags = listOf("Protein"), lowStockThreshold = 200.0,
        nutrition = FoodNutritionDatabase.findBestMatch("chicken breast") ?: NutritionInfo()),
    GroceryItem(id = "demo_2", name = "Spinach", amount = 200.0, unit = "g", category = "Produce",
        tags = listOf("Vegetable", "Iron"), expiryDate = expiryDateInDays(2),
        nutrition = FoodNutritionDatabase.findBestMatch("spinach") ?: NutritionInfo()),
    GroceryItem(id = "demo_3", name = "Milk", amount = 1.0, unit = "l", category = "Dairy",
        tags = listOf("Dairy"), lowStockThreshold = 0.5,
        nutrition = FoodNutritionDatabase.findBestMatch("milk") ?: NutritionInfo()),
    GroceryItem(id = "demo_4", name = "Oats", amount = 500.0, unit = "g", category = "Pantry",
        tags = listOf("Fibre"), nutrition = FoodNutritionDatabase.findBestMatch("oats") ?: NutritionInfo()),
    GroceryItem(id = "demo_5", name = "Salmon", amount = 300.0, unit = "g", category = "Meat & Seafood",
        tags = listOf("Omega-3", "Protein"), expiryDate = expiryDateInDays(1),
        nutrition = FoodNutritionDatabase.findBestMatch("salmon") ?: NutritionInfo()),
    GroceryItem(id = "demo_6", name = "Greek Yogurt", amount = 0.5, unit = "kg", category = "Dairy",
        tags = listOf("Probiotics", "Protein"), lowStockThreshold = 0.2,
        nutrition = FoodNutritionDatabase.findBestMatch("greek yogurt") ?: NutritionInfo())
)

private fun expiryDateInDays(days: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
    return java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
}