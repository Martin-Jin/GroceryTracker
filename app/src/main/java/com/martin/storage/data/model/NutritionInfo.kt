package com.martin.storage.data.model

import kotlinx.serialization.Serializable

/** All values are per 100g / 100ml of the raw ingredient. */
@Serializable
data class NutritionInfo(
    val calories: Double = 0.0,    // kcal
    val protein: Double = 0.0,     // g
    val carbs: Double = 0.0,       // g
    val fat: Double = 0.0,         // g
    val fiber: Double = 0.0,       // g
    val sugar: Double = 0.0,       // g
    val vitaminC: Double = 0.0,    // mg
    val iron: Double = 0.0,        // mg
    val calcium: Double = 0.0,     // mg
    val vitaminD: Double = 0.0,    // mcg
    val vitaminA: Double = 0.0,    // mcg RAE
    val magnesium: Double = 0.0,   // mg
    val zinc: Double = 0.0         // mg
) {
    /** Scale nutrition to a given gram/ml amount. */
    fun forAmount(grams: Double): NutritionInfo {
        val f = grams / 100.0
        return NutritionInfo(
            calories * f, protein * f, carbs * f, fat * f, fiber * f, sugar * f,
            vitaminC * f, iron * f, calcium * f, vitaminD * f, vitaminA * f, magnesium * f, zinc * f
        )
    }

    operator fun plus(other: NutritionInfo) = NutritionInfo(
        calories + other.calories, protein + other.protein, carbs + other.carbs,
        fat + other.fat, fiber + other.fiber, sugar + other.sugar,
        vitaminC + other.vitaminC, iron + other.iron, calcium + other.calcium,
        vitaminD + other.vitaminD, vitaminA + other.vitaminA, magnesium + other.magnesium,
        zinc + other.zinc
    )

    operator fun times(factor: Double) = NutritionInfo(
        calories * factor, protein * factor, carbs * factor, fat * factor,
        fiber * factor, sugar * factor, vitaminC * factor, iron * factor,
        calcium * factor, vitaminD * factor, vitaminA * factor, magnesium * factor, zinc * factor
    )
}

/** Built-in nutrition lookup table (per 100 g/ml). */
object FoodNutritionDatabase {
    private val db = mapOf(
        "spinach"         to NutritionInfo(23.0,  2.9, 3.6,  0.4, 2.2,  0.4, 28.1, 2.71, 99.0,  0.0, 469.0, 79.0, 0.53),
        "banana"          to NutritionInfo(89.0,  1.1,23.0,  0.3, 2.6, 12.2,  8.7, 0.26,  5.0,  0.0,   3.0, 27.0, 0.15),
        "apple"           to NutritionInfo(52.0,  0.3,14.0,  0.2, 2.4, 10.3,  4.6, 0.12,  6.0,  0.0,   3.0,  5.0, 0.04),
        "chicken breast"  to NutritionInfo(165.0,31.0, 0.0,  3.6, 0.0,  0.0,  0.0, 1.04, 15.0,  0.1,   9.0, 29.0, 1.02),
        "salmon"          to NutritionInfo(208.0,20.1, 0.0, 13.4, 0.0,  0.0,  0.0, 0.80,  9.0, 11.1,  58.0, 27.0, 0.36),
        "rice"            to NutritionInfo(130.0, 2.7,28.2,  0.3, 0.4,  0.0,  0.0, 0.20, 10.0,  0.0,   0.0, 12.0, 0.49),
        "broccoli"        to NutritionInfo( 34.0, 2.8, 7.0,  0.4, 2.6,  1.7, 89.2, 0.73, 47.0,  0.0,  31.0, 21.0, 0.41),
        "egg"             to NutritionInfo(155.0,12.6, 1.1, 10.6, 0.0,  1.1,  0.0, 1.83, 56.0,  2.0, 149.0, 12.0, 1.29),
        "milk"            to NutritionInfo( 42.0, 3.4, 5.0,  1.0, 0.0,  5.0,  0.2, 0.03,113.0,  0.1,  46.0, 10.0, 0.37),
        "oats"            to NutritionInfo(389.0,16.9,66.3,  6.9,10.6,  0.0,  0.0, 4.72, 54.0,  0.0,   0.0,177.0, 3.97),
        "avocado"         to NutritionInfo(160.0, 2.0, 8.5, 14.7, 6.7,  0.7, 10.0, 0.61, 12.0,  0.0,   7.0, 29.0, 0.64),
        "lemon"           to NutritionInfo( 29.0, 1.1, 9.3,  0.3, 2.8,  2.5, 53.0, 0.60, 26.0,  0.0,   1.0,  8.0, 0.06),
        "greek yogurt"    to NutritionInfo( 59.0, 9.9, 3.6,  0.4, 0.0,  3.2,  0.5, 0.10,110.0,  0.0,  11.0, 11.0, 0.52),
        "tomato"          to NutritionInfo( 18.0, 0.9, 3.9,  0.2, 1.2,  2.6, 13.7, 0.27, 10.0,  0.0,  42.0, 11.0, 0.17),
        "potato"          to NutritionInfo( 77.0, 2.0,17.5,  0.1, 2.2,  0.8, 19.7, 0.81, 12.0,  0.0,   1.0, 23.0, 0.30),
        "carrot"          to NutritionInfo( 41.0, 0.9, 9.6,  0.2, 2.8,  4.7,  5.9, 0.30, 33.0,  0.0, 835.0, 12.0, 0.24),
        "onion"           to NutritionInfo( 40.0, 1.1, 9.3,  0.1, 1.7,  4.2,  7.4, 0.21, 23.0,  0.0,   0.0, 10.0, 0.17),
        "garlic"          to NutritionInfo(149.0, 6.4,33.1,  0.5, 2.1,  1.0, 31.2, 1.70,181.0,  0.0,   0.0, 25.0, 1.16),
        "bread"           to NutritionInfo(265.0, 9.0,51.0,  3.2, 2.7,  5.0,  0.0, 3.60,260.0,  0.0,   0.0, 26.0, 0.79),
        "pasta"           to NutritionInfo(131.0, 5.0,25.1,  1.1, 1.8,  0.6,  0.0, 1.30, 7.0,   0.0,   0.0, 18.0, 0.51),
        "beef mince"      to NutritionInfo(217.0,17.8, 0.0, 15.3, 0.0,  0.0,  0.0, 2.10, 18.0,  0.1,   0.0, 20.0, 4.05),
        "tuna"            to NutritionInfo(132.0,28.0, 0.0,  1.0, 0.0,  0.0,  0.0, 1.30, 16.0,  4.0,  20.0, 32.0, 0.90),
        "butter"          to NutritionInfo(717.0, 0.9, 0.1, 81.1, 0.0,  0.1,  0.0, 0.02, 24.0,  0.0, 684.0,  2.0, 0.09),
        "olive oil"       to NutritionInfo(884.0, 0.0, 0.0, 100.0,0.0,  0.0,  0.0, 0.56,  1.0,  0.0,   0.0,  0.0, 0.00),
        "cheddar cheese"  to NutritionInfo(402.0,25.0, 1.3, 33.1, 0.0,  0.5,  0.0, 0.68,721.0,  0.6, 265.0, 28.0, 3.11),
        "lentils"         to NutritionInfo(116.0, 9.0,20.1,  0.4, 7.9,  1.8,  1.5, 3.30, 19.0,  0.0,   3.0, 36.0, 1.27),
        "blueberries"     to NutritionInfo( 57.0, 0.7,14.5,  0.3, 2.4,  9.9,  9.7, 0.28,  6.0,  0.0,   3.0,  6.0, 0.16),
        "strawberries"    to NutritionInfo( 32.0, 0.7, 7.7,  0.3, 2.0,  4.9, 58.8, 0.41, 16.0,  0.0,   1.0, 13.0, 0.14),
        "almonds"         to NutritionInfo(579.0,21.2,21.6, 49.9,12.5,  3.9,  0.0, 3.71,264.0,  0.0,   0.0,270.0, 3.12),
        "sweet potato"    to NutritionInfo( 86.0, 1.6,20.1,  0.1, 3.0,  4.2, 19.6, 0.69, 30.0,  0.0, 961.0, 25.0, 0.30)
    )

    fun findBestMatch(name: String): NutritionInfo? {
        val lower = name.lowercase().trim()
        // Exact match first
        db[lower]?.let { return it }
        // Partial match
        return db.entries.firstOrNull { (key, _) ->
            lower.contains(key) || key.contains(lower)
        }?.value
    }
}