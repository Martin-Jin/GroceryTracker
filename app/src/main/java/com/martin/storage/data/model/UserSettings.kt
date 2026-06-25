package com.martin.storage.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    // Daily nutrition goals
    val dailyCalories: Double   = 2000.0,
    val dailyProtein: Double    = 150.0,
    val dailyCarbs: Double      = 250.0,
    val dailyFat: Double        = 65.0,
    val dailyFiber: Double      = 25.0,
    val dailySugar: Double      = 50.0,
    val dailyVitaminC: Double   = 90.0,
    val dailyIron: Double       = 18.0,
    val dailyCalcium: Double    = 1000.0,
    val dailyVitaminD: Double   = 20.0,
    val dailyVitaminA: Double   = 900.0,
    val dailyMagnesium: Double  = 420.0,
    val dailyZinc: Double       = 11.0,
    // Notifications
    val lowStockNotificationsEnabled: Boolean = true,
    val expiryNotificationsEnabled: Boolean   = true,
    val expiryWarningDays: Int                = 3,
    // Shopping list
    val autoGenerateShoppingList: Boolean     = true,
    val defaultLowStockThreshold: Double      = 1.0,
    // Preferences
    val firstLaunchSeeded: Boolean            = false,
    val customCategories: List<String>        = emptyList()
)