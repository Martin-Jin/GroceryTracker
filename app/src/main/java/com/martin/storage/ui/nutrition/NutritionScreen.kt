package com.martin.storage.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martin.storage.data.model.GroceryItem
import com.martin.storage.data.model.NutritionInfo
import com.martin.storage.data.model.PreparedMeal
import com.martin.storage.data.model.UserSettings
import com.martin.storage.data.model.sampleGroceryItems
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.CircularProgress
import com.martin.storage.ui.components.FilterChip
import com.martin.storage.ui.components.GlassCard
import com.martin.storage.ui.components.NutrientBar
import com.martin.storage.ui.components.NutrientChip
import com.martin.storage.ui.components.SectionHeader
import com.martin.storage.ui.components.roundedTo1
import com.martin.storage.ui.theme.Error
import com.martin.storage.ui.theme.OnPrimaryContainer
import com.martin.storage.ui.theme.OnSurface
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.PrimaryContainer
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerHighest
import com.martin.storage.ui.theme.SurfaceContainerLowest
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import com.martin.storage.ui.theme.VitalityFluxTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    repository: AppRepository,
    onNavigateToSettings: () -> Unit,
    viewModel: NutritionViewModel = viewModel(factory = NutritionViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                Modifier.fillMaxWidth().background(Surface.copy(alpha = 0.97f)).statusBarsPadding()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nutrition", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Tune, "Settings", tint = OnSurfaceVariant)
                    }
                }
                // Period selector
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionPeriod.entries.forEach { period ->
                        FilterChip(period.label, state.selectedPeriod == period, onClick = { viewModel.setPeriod(period) })
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Hero calorie card
                CalorieSummaryCard(state)

                // Macros
                SectionHeader("Macros")
                MacroCard(state)

                // Micronutrients
                SectionHeader("Micronutrients")
                MicroNutrientGrid(state)

                // Smart suggestions
                SectionHeader(
                    title = "Boost Your Nutrition",
                    trailingContent = {
                        Text("From your pantry", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                )
                NutritionSuggestions(state)

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Calorie Summary Card ──────────────────────────────────────────────────────

@Composable
private fun CalorieSummaryCard(state: NutritionUiState) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgress(
                progress = state.calorieProgress,
                size = 100.dp,
                strokeWidth = 10.dp,
                progressColor = Primary,
                trackColor = SurfaceContainerHighest
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.accumulated.calories.roundToInt().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text("kcal", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val target = state.periodTarget.calories
                val remaining = (target - state.accumulated.calories).coerceAtLeast(0.0)
                Text("${state.selectedPeriod.label} Goal", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Text(
                    "${(state.calorieProgress * 100).roundToInt()}% of ${target.roundToInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                NutrientChip(
                    "${remaining.roundToInt()} kcal remaining",
                    color = PrimaryContainer.copy(.25f),
                    textColor = OnPrimaryContainer
                )
                Text("${state.dailyMealCount} meals today", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}

// ── Macro Card ────────────────────────────────────────────────────────────────

@Composable
private fun MacroCard(state: NutritionUiState) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            NutrientBar("Protein", state.accumulated.protein, state.periodTarget.protein, Tertiary, "g")
            NutrientBar("Carbohydrates", state.accumulated.carbs, state.periodTarget.carbs, Secondary, "g")
            NutrientBar("Fat", state.accumulated.fat, state.periodTarget.fat, OnSurfaceVariant, "g")
            NutrientBar("Fibre", state.accumulated.fiber, state.periodTarget.fiber, Primary, "g")
        }
    }
}

// ── Micronutrient Grid ────────────────────────────────────────────────────────

@Composable
private fun MicroNutrientGrid(state: NutritionUiState) {
    val acc = state.accumulated
    val tgt = state.periodTarget
    val micros = listOf(
        Triple("Vitamin C", "${acc.vitaminC.roundedTo1()}mg / ${tgt.vitaminC.roundedTo1()}mg", acc.vitaminC / tgt.vitaminC.coerceAtLeast(1.0)) to Tertiary,
        Triple("Iron", "${acc.iron.roundedTo1()}mg / ${tgt.iron.roundedTo1()}mg", acc.iron / tgt.iron.coerceAtLeast(1.0)) to Error,
        Triple("Calcium", "${acc.calcium.roundedTo1()}mg / ${tgt.calcium.roundedTo1()}mg", acc.calcium / tgt.calcium.coerceAtLeast(1.0)) to Primary,
        Triple("Vitamin D", "${acc.vitaminD.roundedTo1()}mcg / ${tgt.vitaminD.roundedTo1()}mcg", acc.vitaminD / tgt.vitaminD.coerceAtLeast(1.0)) to Secondary,
        Triple("Vitamin A", "${acc.vitaminA.roundedTo1()}mcg / ${tgt.vitaminA.roundedTo1()}mcg", acc.vitaminA / tgt.vitaminA.coerceAtLeast(1.0)) to TertiaryContainer,
        Triple("Magnesium", "${acc.magnesium.roundedTo1()}mg / ${tgt.magnesium.roundedTo1()}mg", acc.magnesium / tgt.magnesium.coerceAtLeast(1.0)) to OnSurfaceVariant,
        Triple("Zinc", "${acc.zinc.roundedTo1()}mg / ${tgt.zinc.roundedTo1()}mg", acc.zinc / tgt.zinc.coerceAtLeast(1.0)) to Secondary,
        Triple("Fibre", "${acc.fiber.roundedTo1()}g / ${tgt.fiber.roundedTo1()}g", acc.fiber / tgt.fiber.coerceAtLeast(1.0)) to Primary,
    )

    // 2-column grid
    val rows = micros.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (data, color) ->
                    val (label, value, pct) = data
                    MicroNutrientCard(label, value, pct.coerceIn(0.0, 1.0).toFloat(), color, Modifier.weight(1f))
                }
                // pad last row if odd count
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MicroNutrientCard(label: String, value: String, progress: Float, color: Color, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            Modifier.padding(12.dp).height(90.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                Text(value, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(100.dp)).background(color.copy(.12f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(100.dp)).background(color)
                    )
                }
            }
        }
    }
}

// ── Nutrition Suggestions ─────────────────────────────────────────────────────

@Composable
private fun NutritionSuggestions(state: NutritionUiState) {
    val suggestions = listOf(
        SuggestionData("Low on Iron?", "🩸 Top iron sources in your pantry:", Error) {
            state.topItemsForNutrient { it.iron }
        },
        SuggestionData("Need more Vitamin C?", "🌿 Vitamin C-rich items you have:", Tertiary) {
            state.topItemsForNutrient { it.vitaminC }
        },
        SuggestionData("Boost Calcium", "🦴 Calcium sources in stock:", Primary) {
            state.topItemsForNutrient { it.calcium }
        }
    )

    val gaped = suggestions.filter { it.items().isNotEmpty() }

    if (gaped.isEmpty()) {
        GlassCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Tertiary)
                Text("Great nutrition balance! Add meals to see suggestions.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            gaped.forEach { suggestion ->
                SuggestionCard(suggestion)
            }
        }
    }
}

private data class SuggestionData(
    val title: String,
    val subtitle: String,
    val color: Color,
    val items: () -> List<Pair<GroceryItem, Double>>
)

@Composable
private fun SuggestionCard(suggestion: SuggestionData) {
    val items = suggestion.items()
    if (items.isEmpty()) return

    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(suggestion.color.copy(.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.TipsAndUpdates, null, Modifier.size(18.dp), tint = suggestion.color)
                }
                Column {
                    Text(suggestion.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
                    Text(suggestion.subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
            items.forEach { (item, amount) ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SurfaceContainerLowest).padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${item.amount.roundedTo1()} ${item.unit} in stock", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        NutrientChip(
                            amount.roundedTo1(),
                            color = suggestion.color.copy(.15f),
                            textColor = suggestion.color
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Nutrition Screen")
@Composable
private fun NutritionPreview() {
    VitalityFluxTheme {
        val sampleState = NutritionUiState(
            settings = UserSettings(),
            preparedMeals = listOf(
                PreparedMeal(date = java.text.SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale).format(java.util.Date()),
                    recipeId = "1", recipeName = "Chicken Stir Fry", servings = 1.0,
                    nutritionConsumed = NutritionInfo(480.0, 44.0, 55.0, 8.5, 5.0, 4.0, 48.0, 3.2, 88.0, 0.5, 520.0, 62.0, 2.7))
            ),
            inventoryItems = sampleGroceryItems,
            isLoading = false
        )
        Column(Modifier.fillMaxSize().background(Surface).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Nutrition", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
            CalorieSummaryCard(sampleState)
            MacroCard(sampleState)
        }
    }
}