package com.martin.storage.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.inventory.InventoryScreen
import com.martin.storage.ui.meals.MealsScreen
import com.martin.storage.ui.meals.RecipeDetailScreen
import com.martin.storage.ui.meals.RecipeEditScreen
import com.martin.storage.ui.nutrition.NutritionScreen
import com.martin.storage.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Inventory     : Screen("inventory")
    object Meals         : Screen("meals")
    object Nutrition     : Screen("nutrition")
    object Settings      : Screen("settings")
    object RecipeDetail  : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe/$recipeId"
    }
    object RecipeEdit    : Screen("recipe_edit?recipeId={recipeId}") {
        fun createRoute(recipeId: String? = null) =
            if (recipeId != null) "recipe_edit?recipeId=$recipeId" else "recipe_edit"
    }
}

val bottomNavItems = listOf(
    BottomNavItem("Groceries", "🛒", Screen.Inventory),
    BottomNavItem("Meals",     "🍽️", Screen.Meals),
    BottomNavItem("Nutrition", "📊", Screen.Nutrition)
)

data class BottomNavItem(val label: String, val emoji: String, val screen: Screen)

@Composable
fun AppNavGraph(
    repository: AppRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController   = navController,
        startDestination = Screen.Inventory.route,
        modifier        = modifier,
        enterTransition  = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 } },
        exitTransition   = { fadeOut(tween(150)) },
        popEnterTransition  = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 8 } },
        popExitTransition   = { fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { it / 8 } }
    ) {
        composable(Screen.Inventory.route) {
            InventoryScreen(
                repository  = repository,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Meals.route) {
            MealsScreen(
                repository      = repository,
                onOpenRecipe    = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                onEditRecipe    = { id -> navController.navigate(Screen.RecipeEdit.createRoute(id)) },
                onAddRecipe     = { navController.navigate(Screen.RecipeEdit.createRoute()) }
            )
        }
        composable(Screen.Nutrition.route) {
            NutritionScreen(
                repository           = repository,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                repository = repository,
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { back ->
            val recipeId = back.arguments?.getString("recipeId") ?: return@composable
            RecipeDetailScreen(
                repository   = repository,
                recipeId     = recipeId,
                onNavigateUp = { navController.popBackStack() },
                onEdit       = { navController.navigate(Screen.RecipeEdit.createRoute(recipeId)) }
            )
        }
        composable(
            route     = Screen.RecipeEdit.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { back ->
            val recipeId = back.arguments?.getString("recipeId")
            RecipeEditScreen(
                repository   = repository,
                recipeId     = recipeId,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}