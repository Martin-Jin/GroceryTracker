package com.martin.storage.ui.meals

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martin.storage.data.model.FoodNutritionDatabase
import com.martin.storage.data.model.MealType
import com.martin.storage.data.model.NutritionInfo
import com.martin.storage.data.model.Recipe
import com.martin.storage.data.model.RecipeIngredient
import com.martin.storage.data.model.sampleRecipes
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.FilterChip
import com.martin.storage.ui.components.GlassCard
import com.martin.storage.ui.components.NutrientBar
import com.martin.storage.ui.components.NutrientChip
import com.martin.storage.ui.components.QuantityStepper
import com.martin.storage.ui.components.SectionHeader
import com.martin.storage.ui.components.VitalityTopBar
import com.martin.storage.ui.components.roundedTo1
import com.martin.storage.ui.theme.Error
import com.martin.storage.ui.theme.OnPrimary
import com.martin.storage.ui.theme.OnSurface
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.OutlineVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.SecondaryContainer
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerHigh
import com.martin.storage.ui.theme.SurfaceContainerLowest
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import com.martin.storage.ui.theme.VitalityFluxTheme
import com.martin.storage.data.model.LocalFoodItem

// ── Recipe Detail ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    repository: AppRepository,
    recipeId: String,
    onNavigateUp: () -> Unit,
    onEdit: () -> Unit,
    viewModel: MealsViewModel = viewModel(factory = MealsViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val recipe = state.recipes.find { it.id == recipeId } ?: return
    val canMake = viewModel.canMakeRecipe(recipe)
    val missing = viewModel.missingIngredients(recipe)
    val scrollState = rememberScrollState()
    var servings by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            VitalityTopBar(
                title = recipe.name,
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Primary) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header info
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip("⏱ ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min")
                InfoChip("👤 ${recipe.servings} servings")
                NutrientChip(
                    if (canMake) "✓ Ready to Cook" else "⚠ Missing ${missing.size}",
                    color = if (canMake) TertiaryContainer.copy(.3f) else SecondaryContainer.copy(.3f),
                    textColor = if (canMake) Tertiary else Secondary
                )
            }

            if (recipe.description.isNotBlank()) {
                Text(recipe.description, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            }

            if (recipe.tags.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recipe.tags.forEach { tag -> NutrientChip(tag) }
                }
            }

            // Nutrition card
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nutrition (per serving)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        NutritionStat("${recipe.nutritionPerServing.calories.toInt()}", "kcal", Primary)
                        NutritionStat("${recipe.nutritionPerServing.protein.roundedTo1()}g", "Protein", Tertiary)
                        NutritionStat("${recipe.nutritionPerServing.carbs.roundedTo1()}g", "Carbs", Secondary)
                        NutritionStat("${recipe.nutritionPerServing.fat.roundedTo1()}g", "Fat", OnSurfaceVariant)
                    }
                }
            }

            // Ingredients
            SectionHeader("Ingredients")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recipe.ingredients.forEach { ing ->
                        val scaledAmount = ing.amount * servings / recipe.servings
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(if (ing.optional) OutlineVariant else Primary).align(Alignment.CenterVertically))
                                Text(
                                    ing.itemName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (missing.any { it.itemName == ing.itemName }) Error else OnSurface
                                )
                                if (ing.optional) Text("(opt)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                            Text("${scaledAmount.roundedTo1()} ${ing.unit}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        }
                    }
                }
            }

            if (missing.isNotEmpty()) {
                var addedItems by remember { mutableStateOf(setOf<String>()) }
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.ShoppingCart, null, Modifier.size(18.dp), tint = Secondary)
                            Text(
                                "Missing from inventory",
                                style = MaterialTheme.typography.titleSmall,
                                color = Secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        missing.forEach { ing ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "• ${ing.itemName}: ${ing.amount.roundedTo1()} ${ing.unit}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (ing.itemName in addedItems) {
                                    NutrientChip(
                                        "✓ Added",
                                        color = TertiaryContainer.copy(.3f),
                                        textColor = Tertiary
                                    )
                                } else {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.addMissingIngredientToInventory(ing.itemName)
                                            addedItems = addedItems + ing.itemName
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = SecondaryContainer.copy(alpha = 0.3f),
                                            contentColor = Secondary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add to Inventory", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Instructions
            if (recipe.instructions.isNotEmpty()) {
                SectionHeader("Instructions")
                recipe.instructions.forEachIndexed { idx, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", style = MaterialTheme.typography.labelMedium, color = OnPrimary, fontWeight = FontWeight.Bold)
                        }
                        Text(step, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Servings scaler + cook button
            GlassCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Servings to cook:", style = MaterialTheme.typography.bodyMedium)
                    QuantityStepper(
                        value = servings.toDouble(),
                        unit = "",
                        onIncrease = { servings++ },
                        onDecrease = { if (servings > 1) servings-- }
                    )
                }
            }

            if (canMake) {
                Button(
                    onClick = { viewModel.logMealAsCooked(recipe, servings.toDouble()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Cooked & Update Inventory")
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh).padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
    }
}

@Composable
private fun NutritionStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

// ── Recipe Edit ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    repository: AppRepository,
    recipeId: String?,
    onNavigateUp: () -> Unit,
    viewModel: MealsViewModel = viewModel(factory = MealsViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val existing = recipeId?.let { id -> state.recipes.find { it.id == id } }

    var name        by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var servings    by remember(existing) { mutableStateOf(existing?.servings?.toString() ?: "2") }
    var prepTime    by remember(existing) { mutableStateOf(existing?.prepTimeMinutes?.toString() ?: "10") }
    var cookTime    by remember(existing) { mutableStateOf(existing?.cookTimeMinutes?.toString() ?: "20") }
    var tagsRaw     by remember(existing) { mutableStateOf(existing?.tags?.joinToString(", ") ?: "") }
    var mealTypes   by remember(existing) { mutableStateOf(existing?.mealTypes?.toSet() ?: setOf(MealType.DINNER.name)) }
    var ingredients by remember(existing) { mutableStateOf(existing?.ingredients ?: emptyList()) }
    var instructions by remember(existing) { mutableStateOf(existing?.instructions ?: emptyList()) }
    var newStep     by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val isValid = name.isNotBlank()

    Scaffold(
        topBar = {
            VitalityTopBar(
                title = if (existing == null) "New Recipe" else "Edit Recipe",
                onNavigateUp = onNavigateUp,
                actions = {
                    TextButton(
                        onClick = {
                            if (!isValid) return@TextButton
                            val tags = tagsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            // Compute aggregate nutrition from ingredients
                            val nutrition = ingredients.fold(NutritionInfo()) { acc, ing ->
                                val gramsEquivalent = when (ing.unit.lowercase()) {
                                    "kg", "l" -> ing.amount * 1000
                                    "oz"      -> ing.amount * 28.35
                                    "lbs"     -> ing.amount * 453.6
                                    else      -> ing.amount
                                }
                                val n = FoodNutritionDatabase.findBestMatch(ing.itemName)?.forAmount(gramsEquivalent) ?: NutritionInfo()
                                acc + n
                            }
                            val servingsCount = servings.toIntOrNull() ?: 2
                            val perServing = if (servingsCount > 0) nutrition * (1.0 / servingsCount) else nutrition

                            viewModel.upsertRecipe(
                                Recipe(
                                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    description = description.trim(),
                                    servings = servingsCount,
                                    prepTimeMinutes = prepTime.toIntOrNull() ?: 10,
                                    cookTimeMinutes = cookTime.toIntOrNull() ?: 20,
                                    ingredients = ingredients,
                                    instructions = instructions,
                                    tags = tags,
                                    mealTypes = mealTypes.toList(),
                                    nutritionPerServing = perServing
                                )
                            )
                            onNavigateUp()
                        },
                        enabled = isValid
                    ) {
                        Text("Save", color = if (isValid) Primary else OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(scrollState).padding(padding).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Recipe Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(servings, { servings = it }, label = { Text("Servings") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(prepTime, { prepTime = it }, label = { Text("Prep (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(cookTime, { cookTime = it }, label = { Text("Cook (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            }

            // Meal type selector
            Text("Meal Types", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.entries.forEach { type ->
                    FilterChip(
                        "${type.emoji} ${type.label}",
                        selected = type.name in mealTypes,
                        onClick = {
                            mealTypes = if (type.name in mealTypes) mealTypes - type.name else mealTypes + type.name
                        }
                    )
                }
            }

            OutlinedTextField(tagsRaw, { tagsRaw = it }, label = { Text("Tags (comma-separated)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            // Ingredients
            SectionHeader("Ingredients", trailingContent = {
                Text("${ingredients.size} added", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            })
            IngredientEditor(ingredients = ingredients, allFoodItems = state.allFoodItems, onChange = { ingredients = it })

            // Instructions
            SectionHeader("Instructions", trailingContent = {
                Text("${instructions.size} steps", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            })
            InstructionEditor(instructions = instructions, newStep = newStep, onNewStepChange = { newStep = it }, onChange = { instructions = it }, onAddStep = {
                if (newStep.isNotBlank()) { instructions = instructions + newStep.trim(); newStep = "" }
            })

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun IngredientEditor(
    ingredients: List<RecipeIngredient>,
    allFoodItems: List<LocalFoodItem>,
    onChange: (List<RecipeIngredient>) -> Unit
) {
    var ingName     by remember { mutableStateOf("") }
    var ingAmount   by remember { mutableStateOf("") }
    var ingUnit     by remember { mutableStateOf("g") }
    var suggestions by remember { mutableStateOf<List<LocalFoodItem>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<LocalFoodItem?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ingredients.forEachIndexed { idx, ing ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLowest)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${ing.itemName} — ${ing.amount.roundedTo1()} ${ing.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onChange(ingredients.toMutableList().also { it.removeAt(idx) }) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp), tint = Error)
                }
            }
        }

        // Add-ingredient row with autocomplete on the name field
        val nameMatchesDataset = allFoodItems.isEmpty() ||
                allFoodItems.any {
                    it.displayName.equals(ingName, ignoreCase = true) ||
                            it.name.equals(ingName, ignoreCase = true)
                }
        val canAdd = ingName.isNotBlank() && ingAmount.isNotBlank() && nameMatchesDataset

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(Modifier.weight(2f)) {
                OutlinedTextField(
                    value = ingName,
                    onValueChange = { newName ->
                        ingName = newName
                        selectedFood = null
                        suggestions = allFoodItems.filter { food ->
                            food.displayName.contains(newName, ignoreCase = true) ||
                                    food.name.contains(newName, ignoreCase = true)
                        }.take(6)
                        showSuggestions = suggestions.isNotEmpty() && newName.isNotBlank()
                    },
                    label = { Text("Ingredient") },
                    placeholder = { Text("Search food…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        if (selectedFood != null) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Tertiary)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showSuggestions,
                    onDismissRequest = { showSuggestions = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    suggestions.forEach { food ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(food.displayName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${food.nutrition.calories.toInt()} kcal · " +
                                                "${food.nutrition.protein.toInt()}g prot · ${food.defaultUnit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                ingName = food.displayName
                                ingUnit = food.defaultUnit
                                selectedFood = food
                                showSuggestions = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                ingAmount, { ingAmount = it },
                label = { Text("Amt") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                ingUnit, { ingUnit = it },
                label = { Text("Unit") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            IconButton(
                onClick = {
                    if (canAdd) {
                        onChange(
                            ingredients + RecipeIngredient(
                                ingName.trim(),
                                ingAmount.toDoubleOrNull() ?: 1.0,
                                ingUnit.trim()
                            )
                        )
                        ingName = ""; ingAmount = ""; selectedFood = null
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (canAdd) Primary else SurfaceContainerHigh)
            ) {
                Icon(Icons.Default.Add, null, tint = if (canAdd) OnPrimary else OnSurfaceVariant)
            }
        }

        // Hint when typed name doesn't match any food in the dataset
        if (ingName.isNotBlank() && allFoodItems.isNotEmpty() && !nameMatchesDataset) {
            Text(
                "⚠ \"$ingName\" isn't in the food dataset — select from the dropdown for nutrition tracking.",
                style = MaterialTheme.typography.labelSmall,
                color = Secondary
            )
        }
    }
}

@Composable
private fun InstructionEditor(
    instructions: List<String>,
    newStep: String,
    onNewStepChange: (String) -> Unit,
    onChange: (List<String>) -> Unit,
    onAddStep: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        instructions.forEachIndexed { idx, step ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SurfaceContainerLowest).padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                    Text("${idx + 1}", style = MaterialTheme.typography.labelSmall, color = OnPrimary, fontWeight = FontWeight.Bold)
                }
                Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { onChange(instructions.toMutableList().also { it.removeAt(idx) }) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Error)
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(newStep, onNewStepChange, label = { Text("New step…") }, minLines = 1, maxLines = 3, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
            IconButton(
                onClick = onAddStep,
                enabled = newStep.isNotBlank(),
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(if (newStep.isNotBlank()) Primary else SurfaceContainerHigh)
            ) {
                Icon(Icons.Default.Add, null, tint = if (newStep.isNotBlank()) OnPrimary else OnSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailPreview() {
    VitalityFluxTheme {
        Column(Modifier.background(Surface).fillMaxSize().padding(20.dp)) {
            Text(sampleRecipes[0].name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(12.dp))
            NutrientBar("Protein", sampleRecipes[0].nutritionPerServing.protein, 150.0, Tertiary, "g")
            Spacer(Modifier.height(8.dp))
            NutrientBar("Calories", sampleRecipes[0].nutritionPerServing.calories, 600.0, Primary, "kcal")
        }
    }
}