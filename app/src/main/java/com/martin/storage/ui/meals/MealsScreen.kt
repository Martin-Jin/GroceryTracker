package com.martin.storage.ui.meals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martin.storage.data.model.MealPlanEntry
import com.martin.storage.data.model.MealType
import com.martin.storage.data.model.PreparedMeal
import com.martin.storage.data.model.Recipe
import com.martin.storage.data.model.sampleRecipes
import com.martin.storage.data.model.todayDateStr
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.EmptyState
import com.martin.storage.ui.components.FilterChip
import com.martin.storage.ui.components.GlassCard
import com.martin.storage.ui.components.NutrientChip
import com.martin.storage.ui.components.TagAwareSearchBar
import com.martin.storage.ui.components.UndoSnackbarHost
import com.martin.storage.ui.theme.Error
import com.martin.storage.ui.theme.OnPrimary
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.PrimaryContainer
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.SecondaryContainer
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerLowest
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import com.martin.storage.ui.theme.VitalityFluxTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

private val tabTitles = listOf("Recipes", "Meal Plan", "Prepared")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    repository: AppRepository,
    onOpenRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    onAddRecipe: () -> Unit,
    viewModel: MealsViewModel = viewModel(factory = MealsViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Surface.copy(alpha = 0.97f))
                    .statusBarsPadding()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Meals & Recipes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
                    if (state.canUndo) {
                        IconButton(onClick = {
                            if (pagerState.currentPage == 0) viewModel.undoRecipes()
                            else viewModel.undoMealPlan()
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.Undo, "Undo", tint = Primary)
                        }
                    }
                }
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Primary,
                    indicator = { tabs ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabs[pagerState.currentPage]),
                            color = Primary
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = pagerState.currentPage == idx,
                            onClick = { scope.launch { pagerState.animateScrollToPage(idx) } },
                            text = { Text(title, fontWeight = if (pagerState.currentPage == idx) FontWeight.SemiBold else FontWeight.Normal) },
                            selectedContentColor = Primary,
                            unselectedContentColor = OnSurfaceVariant
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                ExtendedFloatingActionButton(
                    onClick = onAddRecipe,
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("New Recipe") }
                )
            }
        },
        snackbarHost = { UndoSnackbarHost(snackbar) }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            when (page) {
                0 -> RecipesTab(
                    state = state,
                    onOpenRecipe = onOpenRecipe,
                    onEditRecipe = onEditRecipe,
                    onDeleteRecipe = { id ->
                        viewModel.deleteRecipe(id)
                        scope.launch {
                            val r = snackbar.showSnackbar("Recipe deleted", "Undo", duration = SnackbarDuration.Short)
                            if (r == SnackbarResult.ActionPerformed) viewModel.undoRecipes()
                        }
                    },
                    onCookNow = { recipe ->
                        viewModel.logMealAsCooked(recipe, 1.0)
                        scope.launch { snackbar.showSnackbar("✓ ${recipe.name} logged!") }
                    },
                    onSearchChange = viewModel::setSearch,
                    onFilterMealType = viewModel::setMealTypeFilter,
                    onTagApplied = viewModel::applyTagFilter,
                    onTagRemoved = viewModel::removeTagFilter,
                    canMake = viewModel::canMakeRecipe
                )
                1 -> MealPlanTab(
                    state = state,
                    onRemoveEntry = { id ->
                        viewModel.removeMealPlanEntry(id)
                        scope.launch {
                            val r = snackbar.showSnackbar("Entry removed", "Undo", duration = SnackbarDuration.Short)
                            if (r == SnackbarResult.ActionPerformed) viewModel.undoMealPlan()
                        }
                    },
                    onGenerate = viewModel::generateWeeklyPlan,
                    onClear = viewModel::clearWeekPlan,
                    onAddEntry = { entry -> viewModel.addMealPlanEntry(entry) },
                    recipes = state.recipes
                )
                2 -> PreparedTab(
                    meals = state.recentPrepared,
                    onDelete = viewModel::deletePreparedMeal,
                    onMarkEaten = viewModel::markMealAsEaten
                )
            }
        }
    }
}

// ── Recipes Tab ───────────────────────────────────────────────────────────────

@Composable
private fun RecipesTab(
    state: MealsUiState,
    onOpenRecipe: (String) -> Unit,
    onEditRecipe: (String) -> Unit,
    onDeleteRecipe: (String) -> Unit,
    onCookNow: (Recipe) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterMealType: (String?) -> Unit,
    onTagApplied: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    canMake: (Recipe) -> Boolean
) {
    Column(Modifier.fillMaxSize()) {
        // Search
        TagAwareSearchBar(
            textQuery = state.searchQuery,
            onTextQueryChange = onSearchChange,
            appliedTagFilters = state.appliedTagFilters,
            allAvailableTags = state.allRecipeTags,
            onTagApplied = onTagApplied,
            onTagRemoved = onTagRemoved,
            placeholder = "Search recipes…",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
        )
        // Meal type filter
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("All", state.selectedMealType == null, { onFilterMealType(null) })
            MealType.entries.forEach { type ->
                FilterChip("${type.emoji} ${type.label}", state.selectedMealType == type.name, { onFilterMealType(type.name) })
            }
        }

        if (state.filteredRecipes.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, null, Modifier.size(36.dp), tint = OnSurfaceVariant) },
                title = "No Recipes",
                subtitle = "Add recipes to plan your meals",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        canMake = canMake(recipe),
                        onClick = { onOpenRecipe(recipe.id) },
                        onEdit = { onEditRecipe(recipe.id) },
                        onDelete = { onDeleteRecipe(recipe.id) },
                        onCookNow = { onCookNow(recipe) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    canMake: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCookNow: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(recipe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min · ${recipe.servings} servings",
                        style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Delete", color = Error) }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Error) }, onClick = { showMenu = false; onDelete() })
                    }
                }
            }

            if (recipe.tags.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recipe.tags.take(4).forEach { tag ->
                        NutrientChip(tag, color = TertiaryContainer.copy(.25f), textColor = Tertiary)
                    }
                }
            }

            // Nutrition summary
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MacroStat("${recipe.nutritionPerServing.calories.toInt()} kcal", "Cal", Primary)
                MacroStat("${recipe.nutritionPerServing.protein.toInt()}g", "Protein", Tertiary)
                MacroStat("${recipe.nutritionPerServing.carbs.toInt()}g", "Carbs", Secondary)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NutrientChip(
                    if (canMake) "✓ Can Make" else "⚠ Missing Items",
                    color = if (canMake) TertiaryContainer.copy(.3f) else SecondaryContainer.copy(.3f),
                    textColor = if (canMake) Tertiary else Secondary
                )
                if (canMake) {
                    FilledTonalButton(
                        onClick = onCookNow,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Primary.copy(.12f), contentColor = Primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cook Now", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

// ── Meal Plan Tab ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanTab(
    state: MealsUiState,
    onRemoveEntry: (String) -> Unit,
    onGenerate: () -> Unit,
    onClear: () -> Unit,
    onAddEntry: (MealPlanEntry) -> Unit,
    recipes: List<Recipe>
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGenerate,
                enabled = !state.isGenerating && recipes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.weight(1f)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = OnPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Generate")
                }
            }
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Manual Add")
            }
        }

        if (state.weekPlanByDay.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Outlined.DateRange, null, Modifier.size(36.dp), tint = OnSurfaceVariant) },
                title = "No Meal Plan Yet",
                subtitle = "Generate a plan or add meals manually",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.weekPlanByDay.forEach { (date, entries) ->
                    item(key = "header_$date") {
                        val d = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale).parse(date)
                        val formatted = SimpleDateFormat("EEEE, d MMM", LocalLocale.current.platformLocale).format(d!!)
                        Text(formatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    items(entries, key = { it.id }) { entry ->
                        MealPlanEntryCard(entry = entry, onRemove = { onRemoveEntry(entry.id) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddMealPlanDialog(
            recipes = recipes,
            onAdd = { entry -> onAddEntry(entry); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun MealPlanEntryCard(entry: MealPlanEntry, onRemove: () -> Unit) {
    val mealType = MealType.entries.find { it.name == entry.mealType }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLowest)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer.copy(.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(mealType?.emoji ?: "🍽️", style = MaterialTheme.typography.bodyLarge)
        }
        Column(Modifier.weight(1f)) {
            Text(entry.recipeName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(mealType?.label ?: entry.mealType, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealPlanDialog(
    recipes: List<Recipe>,
    onAdd: (MealPlanEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(todayDateStr()) }
    var selectedMealType by remember { mutableStateOf(MealType.DINNER) }
    var selectedRecipe by remember { mutableStateOf(recipes.firstOrNull()) }
    var recipeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Meal Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            type.emoji,
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type }
                        )
                    }
                }
                ExposedDropdownMenuBox(expanded = recipeExpanded, onExpandedChange = { recipeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedRecipe?.name ?: "Select recipe",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recipe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(recipeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(expanded = recipeExpanded, onDismissRequest = { recipeExpanded = false }) {
                        recipes.forEach { recipe ->
                            DropdownMenuItem(
                                text = { Text(recipe.name) },
                                onClick = { selectedRecipe = recipe; recipeExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = selectedRecipe ?: return@Button
                    onAdd(MealPlanEntry(date = selectedDate, mealType = selectedMealType.name, recipeId = r.id, recipeName = r.name))
                },
                enabled = selectedRecipe != null,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Prepared Tab ──────────────────────────────────────────────────────────────

@Composable
private fun PreparedTab(
    meals: List<PreparedMeal>,
    onDelete: (String) -> Unit,
    onMarkEaten: (String) -> Unit
) {
    if (meals.isEmpty()) {
        EmptyState(
            icon = { Icon(Icons.Outlined.Restaurant, null, Modifier.size(36.dp), tint = OnSurfaceVariant) },
            title = "No Meals Cooked Yet",
            subtitle = "When you cook a recipe, it appears here",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(meals, key = { it.id }) { meal ->
                PreparedMealCard(
                    meal = meal,
                    onDelete = { onDelete(meal.id) },
                    onMarkEaten = { onMarkEaten(meal.id) }
                )
            }
        }
    }
}

@Composable
private fun PreparedMealCard(
    meal: PreparedMeal,
    onDelete: () -> Unit,
    onMarkEaten: () -> Unit
) {
    val daysSince = remember(meal.date) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val d = sdf.parse(meal.date)
            ((System.currentTimeMillis() - (d?.time ?: System.currentTimeMillis())) / 86_400_000L)
                .toInt().coerceAtLeast(0)
        } catch (_: Exception) { 0 }
    }

    GlassCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        meal.recipeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (meal.eaten) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(15.dp), tint = Tertiary)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(meal.date, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text(
                        "· " + when (daysSince) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> "$daysSince days ago"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${meal.nutritionConsumed.calories.toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${meal.nutritionConsumed.protein.toInt()}g protein",
                        style = MaterialTheme.typography.labelSmall,
                        color = Tertiary
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!meal.eaten) {
                    FilledTonalButton(
                        onClick = onMarkEaten,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TertiaryContainer.copy(alpha = 0.4f),
                            contentColor = Tertiary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Eaten", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    NutrientChip("✓ Consumed", color = TertiaryContainer.copy(0.3f), textColor = Tertiary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeCardPreview() {
    VitalityFluxTheme {
        Column(Modifier.fillMaxSize().background(Surface).padding(20.dp)) {
            sampleRecipes.take(2).forEach { recipe ->
                RecipeCard(recipe = recipe, canMake = true, onClick = {}, onEdit = {}, onDelete = {}, onCookNow = {})
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}