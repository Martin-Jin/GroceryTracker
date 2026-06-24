package com.martin.storage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.data.repository.scheduleGroceryChecks
import com.martin.storage.ui.navigation.AppNavGraph
import com.martin.storage.ui.navigation.Screen
import com.martin.storage.ui.navigation.bottomNavItems
import com.martin.storage.ui.theme.Background
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.PrimaryContainer
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.VitalityFluxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository by lazy { AppRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed sample data on first launch, then schedule periodic background checks.
        lifecycleScope.launch { repository.seedIfFirstLaunch() }
        scheduleGroceryChecks(this)

        setContent {
            VitalityFluxTheme {
                GroceryApp(repository = repository)
            }
        }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun GroceryApp(repository: AppRepository) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            AppBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        // The nav graph fills the remaining space above the bottom bar.
        // Settings is a full-screen overlay pushed onto the back stack — it has
        // no bottom bar entry, so we pass it as a separate composable from the
        // Scaffold nav content via NavGraph.
        AppNavGraph(
            repository    = repository,
            navController = navController,
            modifier      = Modifier.padding(innerPadding)
        )
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
private fun AppBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide the bottom bar on screens that are full-screen overlays.
    val hideBarRoutes = setOf(Screen.Settings.route, Screen.RecipeDetail.route, Screen.RecipeEdit.route)
    val currentRoute  = currentDestination?.route ?: ""
    val showBar       = hideBarRoutes.none { currentRoute.startsWith(it.substringBefore("{")) }

    AnimatedVisibility(
        visible = showBar,
        enter   = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
        exit    = slideOutVertically(tween(150)) { it } + fadeOut(tween(150))
    ) {
        NavigationBar(
            modifier       = Modifier.height(72.dp),
            containerColor = Surface.copy(alpha = 0.97f),
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick  = {
                        navController.navigate(item.screen.route) {
                            // Pop to start to avoid building up a back stack of bottom-nav destinations.
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    icon = {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = {
                                scaleIn(tween(150)) + fadeIn(tween(150)) togetherWith
                                        scaleOut(tween(100)) + fadeOut(tween(100))
                            },
                            label = "navIcon"
                        ) { isSelected ->
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .padding(horizontal = 12.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.emoji, style = MaterialTheme.typography.titleLarge)
                                }
                            } else {
                                Text(item.emoji, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    },
                    label = {
                        Text(
                            text       = item.label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            style      = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Primary,
                        selectedTextColor   = Primary,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant,
                        indicatorColor      = PrimaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Bottom Nav Bar")
@Composable
private fun BottomNavPreview() {
    VitalityFluxTheme {
        Surface(color = Surface) {
            NavigationBar(
                modifier       = Modifier.height(72.dp),
                containerColor = Surface.copy(alpha = 0.97f),
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected    = index == 0,
                        onClick     = {},
                        icon        = { Text(item.emoji, style = MaterialTheme.typography.titleMedium) },
                        label       = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        colors      = NavigationBarItemDefaults.colors(
                            selectedTextColor   = Primary,
                            indicatorColor      = PrimaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}