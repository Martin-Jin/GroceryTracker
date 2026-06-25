package com.martin.storage.ui.inventory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martin.storage.data.model.FoodNutritionDatabase
import com.martin.storage.data.model.GroceryItem
import com.martin.storage.data.model.NutritionInfo
import com.martin.storage.data.model.defaultUnits
import com.martin.storage.data.model.groceryCategories
import com.martin.storage.data.model.sampleGroceryItems
import com.martin.storage.data.model.todayDateStr
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.EmptyState
import com.martin.storage.ui.components.ExpiryBadge
import com.martin.storage.ui.components.FilterChip
import com.martin.storage.ui.components.GlassCard
import com.martin.storage.ui.components.NutrientChip
import com.martin.storage.ui.components.QuantityStepper
import com.martin.storage.ui.components.StockIndicator
import com.martin.storage.ui.components.TagAwareSearchBar
import com.martin.storage.ui.components.UndoSnackbarHost
import com.martin.storage.ui.receipt.EXTRA_RECEIPT_ITEMS
import com.martin.storage.ui.receipt.ReceiptScannerActivity
import com.martin.storage.ui.theme.Error
import com.martin.storage.ui.theme.ErrorContainer
import com.martin.storage.ui.theme.OnPrimary
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.SecondaryContainer
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerHigh
import com.martin.storage.ui.theme.SurfaceContainerLowest
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import com.martin.storage.ui.theme.VitalityFluxTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val context = LocalContext.current

    val receiptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val json = result.data?.getStringExtra(EXTRA_RECEIPT_ITEMS) ?: return@rememberLauncherForActivityResult
            try {
                val names: List<String> = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
                names.forEach { name ->
                    val trimmed = name.trim().replaceFirstChar { it.uppercase() }
                    val nutrition = FoodNutritionDatabase.findBestMatch(trimmed) ?: NutritionInfo()
                    viewModel.upsertItem(GroceryItem(name = trimmed, nutrition = nutrition))
                }
            } catch (_: Exception) { }
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) receiptLauncher.launch(Intent(context, ReceiptScannerActivity::class.java))
    }

    fun launchReceiptScanner() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            receiptLauncher.launch(Intent(context, ReceiptScannerActivity::class.java))
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

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
                                Icon(Icons.AutoMirrored.Outlined.Undo, "Undo", tint = Primary)
                            }
                        }
                        if (state.canRedo) {
                            IconButton(onClick = { viewModel.redo() }) {
                                Icon(Icons.AutoMirrored.Outlined.Redo, "Redo", tint = Primary)
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, "Settings", tint = OnSurfaceVariant)
                        }
                    }
                }
                TagAwareSearchBar(
                    textQuery = state.searchQuery,
                    onTextQueryChange = viewModel::setSearch,
                    appliedTagFilters = state.appliedTagFilters,
                    allAvailableTags = state.allTags,
                    onTagApplied = viewModel::applyTagFilter,
                    onTagRemoved = viewModel::removeTagFilter,
                    placeholder = "Search items…",
                    trailingContent = {
                        FilledTonalIconButton(
                            onClick = { showSortSheet = true },
                            modifier = Modifier.size(52.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = SurfaceContainerHigh
                            )
                        ) {
                            Icon(Icons.Default.FilterList, "Sort", tint = Primary)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )
                CategoryFilterRow(
                    allCategories = state.allCategories,
                    customCategories = state.customCategories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = viewModel::setCategory,
                    showShoppingOnly = state.showShoppingListOnly,
                    onToggleShoppingOnly = viewModel::toggleShoppingListOnly,
                    onAddCategory = viewModel::addCategory,
                    onRemoveCategory = viewModel::removeCustomCategory,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        floatingActionButton = {
            InventoryFab(
                onAddItem     = { showAddSheet = true },
                onScanReceipt = { launchReceiptScanner() }
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
                                val result = snackbarHostState.showSnackbar("${item.name} deleted", "Undo", duration = SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) viewModel.undo()
                            }
                        },
                        onIncrease = { viewModel.adjustAmount(item.id, item.portionSize) },
                        onDecrease = { viewModel.adjustAmount(item.id, -item.portionSize) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet || editingItem != null) {
        GroceryItemSheet(
            item = editingItem,
            categories = state.allCategories,
            onSave = { viewModel.upsertItem(it); showAddSheet = false; editingItem = null },
            onDelete = { item ->
                viewModel.deleteItem(item.id)
                scope.launch {
                    val result = snackbarHostState.showSnackbar("${item.name} deleted", "Undo", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.undo()
                }
                showAddSheet = false
                editingItem = null
            },
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

// ── Category Filter Row ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryFilterRow(
    allCategories: List<String>,
    customCategories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    showShoppingOnly: Boolean,
    onToggleShoppingOnly: () -> Unit,
    onAddCategory: (String) -> Unit,
    onRemoveCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            label = "🛒 Shopping",
            selected = showShoppingOnly,
            onClick = onToggleShoppingOnly
        )

        allCategories.forEach { cat ->
            val isCustom = cat in customCategories
            var showDeleteDialog by remember { mutableStateOf(false) }

            Box {
                val bg = if (selectedCategory == cat) Primary else SurfaceContainerHigh
                val textColor = if (selectedCategory == cat) OnPrimary else OnSurfaceVariant

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(bg)
                        .combinedClickable(
                            onLongClick = { if (isCustom) showDeleteDialog = true },
                            onClick = { onCategorySelected(if (selectedCategory == cat) null else cat) }
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(cat, style = MaterialTheme.typography.labelMedium, color = textColor)
                    // Subtle dot to indicate custom (deletable) category
                    if (isCustom) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(textColor.copy(alpha = 0.5f))
                        )
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Remove Category?") },
                        text = { Text("Remove \"$cat\" from your categories? Items already in this category keep their value.") },
                        confirmButton = {
                            TextButton(onClick = {
                                onRemoveCategory(cat)
                                showDeleteDialog = false
                            }) { Text("Remove", color = Error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }

        // Add category button
        FilledTonalIconButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = SurfaceContainerHigh)
        ) {
            Icon(Icons.Default.Add, "Add category", modifier = Modifier.size(16.dp), tint = Primary)
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onAdd = { onAddCategory(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddCategoryDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Category name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onAdd(text.trim()) },
                enabled = text.isNotBlank()
            ) { Text("Add", color = if (text.isNotBlank()) Primary else OnSurfaceVariant) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Grocery List Item ─────────────────────────────────────────────────────────

// Find the entire GroceryListItem function and replace with:
@Composable
private fun GroceryListItem(
    item: GroceryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StockIndicator(item.isLowStock, item.isOutOfStock)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                    item.daysUntilExpiry?.let {
                        if (it <= 5) ExpiryBadge(daysUntilExpiry = it)
                    }
                }
                if (item.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.tags.forEach { tag ->
                            NutrientChip(
                                tag,
                                color = TertiaryContainer.copy(.25f),
                                textColor = Tertiary,
                                modifier = Modifier.widthIn(max = 90.dp)
                            )
                        }
                    }
                }
            }

            QuantityStepper(
                value = item.amount,
                unit = item.unit,
                onIncrease = onIncrease,
                onDecrease = onDecrease
            )
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
    categories: List<String> = groceryCategories,
    onSave: (GroceryItem) -> Unit,
    onDelete: ((GroceryItem) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }
    var amount by remember(item) { mutableStateOf(item?.amount?.toString() ?: "1") }
    var unit by remember(item) { mutableStateOf(item?.unit ?: "units") }
    var category by remember(item) { mutableStateOf(item?.category ?: "General") }
    var tagsRaw by remember(item) { mutableStateOf(item?.tags?.joinToString(", ") ?: "") }
    var expiryDate by remember(item) { mutableStateOf(item?.expiryDate ?: "") }
    var threshold by remember(item) { mutableStateOf(item?.lowStockThreshold?.toString() ?: "1") }
    var portionSize by remember(item) { mutableStateOf(item?.portionSize?.toString() ?: "1") }
    var notes by remember(item) { mutableStateOf(item?.notes ?: "") }

    // Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = remember(item?.expiryDate) {
            val raw = item?.expiryDate ?: ""
            if (raw.isBlank()) null
            else try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(raw)?.time } catch (_: Exception) { null }
        }
    )
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
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categories.forEach { cat ->
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
                onValueChange = { },
                readOnly = true,
                label = { Text("Expiry Date") },
                placeholder = { Text("Tap to select") },
                singleLine = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (expiryDate.isNotBlank()) {
                            IconButton(onClick = { expiryDate = "" }) {
                                Icon(Icons.Default.Clear, "Clear date", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, "Pick date", tint = Primary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                expiryDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

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
                value = portionSize,
                onValueChange = { portionSize = it },
                label = { Text("Portion size (step per +/-)") },
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
                                portionSize = portionSize.toDoubleOrNull()?.coerceAtLeast(0.01) ?: 1.0,
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
            if (item != null && onDelete != null) {
                Button(
                    onClick = { onDelete(item) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("  Delete Item")
                }
            }
        }
    }
}

@Composable
private fun InventoryFab(
    onAddItem: () -> Unit,
    onScanReceipt: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { it },
            exit  = fadeOut(tween(100)) + slideOutVertically(tween(100)) { it }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FabSubAction(label = "Scan Receipt", icon = Icons.Default.Camera) {
                    expanded = false; onScanReceipt()
                }
                FabSubAction(label = "Add Item", icon = Icons.Default.Add) {
                    expanded = false; onAddItem()
                }
            }
        }

        FloatingActionButton(
            onClick        = { expanded = !expanded },
            containerColor = Primary,
            contentColor   = OnPrimary
        ) {
            Icon(
                imageVector        = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun FabSubAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                maxLines = 1
            )
        }
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = Primary,
            contentColor   = OnPrimary
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
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