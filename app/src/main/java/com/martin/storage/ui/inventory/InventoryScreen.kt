package com.martin.storage.ui.inventory

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martin.storage.data.model.*
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.*
import com.martin.storage.ui.navigation.bottomNavItems
import com.martin.storage.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    repository: AppRepository,
    onNavigateToSettings: () -> Unit,
    viewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<GroceryItem?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface.copy(alpha = 0.97f))
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("My Groceries", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
                        if (state.lowStockCount > 0 || state.expiringCount > 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (state.lowStockCount > 0)
                                    NutrientChip("${state.lowStockCount} Low", color = SecondaryContainer.copy(.35f), textColor = Secondary)
                                if (state.expiringCount > 0)
                                    NutrientChip("${state.expiringCount} Expiring", color = ErrorContainer.copy(.5f), textColor = Error)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (state.canUndo) {
                            IconButton(onClick = { viewModel.undo() }) {
                                Icon(Icons.Outlined.Undo, "Undo", tint = Primary)
                            }
                        }
                        if (state.canRedo) {
                            IconButton(onClick = { viewModel.redo() }) {
                                Icon(Icons.Outlined.Redo, "Redo", tint = Primary)
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, "Settings", tint = OnSurfaceVariant)
                        }
                    }
                }
                // Search Bar
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearch,
                    onSortClick = { showSortSheet = true },
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )
                // Filter chips
                AnimatedVisibility(state.allTags.isNotEmpty() || state.selectedCategory != null) {
                    FilterRow(
                        categories = groceryCategories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = viewModel::setCategory,
                        selectedTags = state.selectedTags,
                        availableTags = state.allTags,
                        onTagToggled = viewModel::toggleTag,
                        showShoppingOnly = state.showShoppingListOnly,
                        onToggleShoppingOnly = viewModel::toggleShoppingListOnly,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Primary,
                contentColor = OnPrimary,
                icon = { Icon(Icons.Default.Add, "Add item") },
                text = { Text("Add Item") }
            )
        },
        snackbarHost = { UndoSnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (state.filtered.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Outlined.Inventory2, null, Modifier.size(36.dp), tint = OnSurfaceVariant) },
                title = if (state.searchQuery.isBlank() && state.selectedCategory == null) "No Items Yet" else "No Results",
                subtitle = if (state.searchQuery.isBlank() && state.selectedCategory == null)
                    "Tap + to add your first grocery item" else "Try adjusting your search or filters",
                actionLabel = if (state.searchQuery.isNotBlank() || state.selectedCategory != null) "Clear Filters" else null,
                onAction = viewModel::clearFilters,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.filtered, key = { it.id }) { item ->
                    GroceryListItem(
                        item = item,
                        onEdit = { editingItem = item },
                        onDelete = {
                            viewModel.deleteItem(item.id)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar("${item.name} deleted", "Undo", SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) viewModel.undo()
                            }
                        },
                        onIncrease = { viewModel.adjustAmount(item.id, stepFor(item.unit)) },
                        onDecrease = { viewModel.adjustAmount(item.id, -stepFor(item.unit)) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet || editingItem != null) {
        GroceryItemSheet(
            item = editingItem,
            onSave = { viewModel.upsertItem(it); showAddSheet = false; editingItem = null },
            onDismiss = { showAddSheet = false; editingItem = null }
        )
    }
    if (showSortSheet) {
        SortBottomSheet(
            current = state.sortOrder,
            onSelect = { viewModel.setSortOrder(it); showSortSheet = false },
            onDismiss = { showSortSheet = false }
        )
    }
}

private fun stepFor(unit: String): Double = when (unit.lowercase()) {
    "kg", "l", "lbs" -> 0.1
    "g", "ml", "oz"  -> 50.0
    else              -> 1.0
}

// ── Search Bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search items…", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, null, tint = OnSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = OutlineVariant,
                focusedContainerColor = SurfaceContainerLowest,
                unfocusedContainerColor = SurfaceContainerLowest
            ),
            modifier = Modifier.weight(1f).height(52.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        FilledTonalIconButton(
            onClick = onSortClick,
            modifier = Modifier.size(52.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = SurfaceContainerHigh
            )
        ) {
            Icon(Icons.Default.FilterList, "Sort / Filter", tint = Primary)
        }
    }
}

// ── Filter Row ────────────────────────────────────────────────────────────────

@Composable
private fun FilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    selectedTags: Set<String>,
    availableTags: List<String>,
    onTagToggled: (String) -> Unit,
    showShoppingOnly: Boolean,
    onToggleShoppingOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            label = "🛒 Shopping",
            selected = showShoppingOnly,
            onClick = onToggleShoppingOnly
        )
        categories.forEach { cat ->
            FilterChip(
                label = cat,
                selected = selectedCategory == cat,
                onClick = { onCategorySelected(if (selectedCategory == cat) null else cat) }
            )
        }
        availableTags.forEach { tag ->
            FilterChip(
                label = "#$tag",
                selected = tag in selectedTags,
                onClick = { onTagToggled(tag) }
            )
        }
    }
}

// ── Grocery List Item ─────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryListItem(
    item: GroceryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status dot
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StockIndicator(item.isLowStock, item.isOutOfStock)
            }
            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                    item.daysUntilExpiry?.let {
                        if (it <= 5) ExpiryBadge(daysUntilExpiry = it)
                    }
                }
                if (item.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.tags.take(3).forEach { tag ->
                            NutrientChip(tag, color = TertiaryContainer.copy(.25f), textColor = Tertiary)
                        }
                    }
                }
            }
            // Stepper
            QuantityStepper(
                value = item.amount,
                unit = item.unit,
                onIncrease = onIncrease,
                onDecrease = onDecrease
            )
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ── Sort Bottom Sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    current: SortOrder,
    onSelect: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceContainerLowest) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text("Sort & Order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            SortOrder.entries.forEach { order ->
                ListItem(
                    headlineContent = { Text(order.label) },
                    leadingContent = {
                        RadioButton(selected = current == order, onClick = { onSelect(order) },
                            colors = RadioButtonDefaults.colors(selectedColor = Primary))
                    },
                    modifier = Modifier.clickable { onSelect(order) }.clip(RoundedCornerShape(12.dp)),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

// ── Grocery Item Edit/Add Sheet ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryItemSheet(
    item: GroceryItem?,
    onSave: (GroceryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }
    var amount by remember(item) { mutableStateOf(item?.amount?.toString() ?: "1") }
    var unit by remember(item) { mutableStateOf(item?.unit ?: "units") }
    var category by remember(item) { mutableStateOf(item?.category ?: "General") }
    var tagsRaw by remember(item) { mutableStateOf(item?.tags?.joinToString(", ") ?: "") }
    var expiryDate by remember(item) { mutableStateOf(item?.expiryDate ?: "") }
    var threshold by remember(item) { mutableStateOf(item?.lowStockThreshold?.toString() ?: "1") }
    var notes by remember(item) { mutableStateOf(item?.notes ?: "") }
    var unitExpanded by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (item == null) "Add Item" else "Edit Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        defaultUnits.forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    groceryCategories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; catExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = tagsRaw,
                onValueChange = { tagsRaw = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("e.g. Protein, Vegetable") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (dd/MM/yyyy)") },
                placeholder = { Text("Leave blank if none") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("Low-stock Threshold") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            val isValid = name.isNotBlank()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        val tags = tagsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val nutrition = FoodNutritionDatabase.findBestMatch(name) ?: item?.nutrition ?: NutritionInfo()
                        onSave(
                            GroceryItem(
                                id = item?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name.trim(),
                                amount = amount.toDoubleOrNull() ?: 1.0,
                                unit = unit,
                                category = category,
                                tags = tags,
                                expiryDate = expiryDate.trim(),
                                lowStockThreshold = threshold.toDoubleOrNull() ?: 1.0,
                                notes = notes.trim(),
                                addedDate = item?.addedDate ?: todayDateStr(),
                                nutrition = nutrition
                            )
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Save") }
            }
        }
    }
}

@Preview(showBackground = true, name = "Inventory Screen")
@Composable
private fun InventoryPreview() {
    VitalityFluxTheme {
        // Stub preview
        Column(
            Modifier.fillMaxSize().background(Surface).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("My Groceries", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
            sampleGroceryItems.take(3).forEach { item ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text("${item.amount} ${item.unit}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                        StockIndicator(item.isLowStock, item.isOutOfStock)
                    }
                }
            }
        }
    }
}