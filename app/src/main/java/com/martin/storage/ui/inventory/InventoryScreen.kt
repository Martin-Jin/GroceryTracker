package com.martin.storage.ui.inventory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martin.storage.data.model.FoodNutritionDatabase
import com.martin.storage.data.model.GroceryItem
import com.martin.storage.data.model.LocalFoodItem
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
import com.martin.storage.ui.theme.OnSurface
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
import com.martin.storage.ui.components.AddCategoryDialog
import com.martin.storage.ui.components.EditCategoryDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    repository: AppRepository,
    onNavigateToSettings: () -> Unit,
    viewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<GroceryItem?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<GroceryItem?>(null) }

    LaunchedEffect(pendingDelete) {
        val item = pendingDelete ?: return@LaunchedEffect
        viewModel.deleteItem(item.id)
        val result = snackbarHostState.showSnackbar(
            message = "${item.name} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undo()
        }
        pendingDelete = null
    }

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
                    onRenameCategory = viewModel::renameCategory,
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
                    val dismissState = rememberSwipeToDismissBoxState()

                    // Reset swipe state if item reappears (e.g. after undo)
                    LaunchedEffect(item.id) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                        }
                    }

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            pendingDelete = item
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Error.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                },
                                label = "swipeBg"
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(color)
                                    .padding(end = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = Error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    ) {
                        GroceryListItem(
                            item = item,
                            onEdit = { editingItem = item },
                            onIncrease = { viewModel.adjustAmount(item.id, item.portionSize) },
                            onDecrease = { viewModel.adjustAmount(item.id, -item.portionSize) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet || editingItem != null) {
        GroceryItemSheet(
            item = editingItem,
            allFoodItems = state.allFoodItems,
            allCategories = state.allCategories,
            onSave = { viewModel.upsertItem(it); showAddSheet = false; editingItem = null },
            onUpsertFoodItem = { viewModel.upsertLocalFoodItem(it) },
            onDeleteFoodItem = { viewModel.deleteLocalFoodItem(it) },
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
    onRenameCategory: (String, String) -> Unit,   // ← ADD
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
            var showEditDialog by remember { mutableStateOf(false) }

            val isSelected = selectedCategory == cat
            val bg = if (isSelected) Primary else SurfaceContainerHigh
            val textColor = if (isSelected) OnPrimary else OnSurfaceVariant

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(bg)
                        .combinedClickable(
                            onLongClick = { if (isCustom) showDeleteDialog = true },
                            onClick = { onCategorySelected(if (isSelected) null else cat) }
                        )
                        .padding(start = 14.dp, end = if (isCustom) 6.dp else 14.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        cat,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                    // Edit pen inside the pill for custom categories
                    if (isCustom) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                                .clickable { showEditDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Rename $cat",
                                tint = textColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }
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

                if (showEditDialog) {
                    EditCategoryDialog(
                        current = cat,
                        onSave = { newName ->
                            onRenameCategory(cat, newName)
                            showEditDialog = false
                        },
                        onDismiss = { showEditDialog = false }
                    )
                }
            }
        }

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
// ── Grocery List Item ─────────────────────────────────────────────────────────
@Composable
private fun GroceryListItem(
    item: GroceryItem,
    onEdit: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 5.dp)
                .heightIn(min = 65.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StockIndicator(item.isLowStock, item.isOutOfStock)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Name row
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Always-present subtitle row: category + optional expiry badge inline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                    item.daysUntilExpiry?.let { days ->
                        if (days <= 5) ExpiryBadge(daysUntilExpiry = days)
                    }
                }
                // Tags row — always occupies same height via a fixed-height box
                Box(modifier = Modifier.height(22.dp)) {
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
    allFoodItems: List<LocalFoodItem>,
    allCategories: List<String>,
    onSave: (GroceryItem) -> Unit,
    onUpsertFoodItem: (LocalFoodItem) -> Unit,
    onDeleteFoodItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name        by remember(item) { mutableStateOf(item?.name ?: "") }
    var amount      by remember(item) { mutableStateOf(item?.amount?.toString() ?: "1") }
    var unit        by remember(item) { mutableStateOf(item?.unit ?: "units") }
    var category    by remember(item) { mutableStateOf(item?.category ?: "General") }
    var tagsRaw     by remember(item) { mutableStateOf(item?.tags?.joinToString(", ") ?: "No tag") }
    var expiryDate  by remember(item) { mutableStateOf(item?.expiryDate ?: "") }
    var threshold   by remember(item) { mutableStateOf(item?.lowStockThreshold?.toString() ?: "1") }
    var notes       by remember(item) { mutableStateOf(item?.notes ?: "") }
    var nutritionState by remember(item) { mutableStateOf(item?.nutrition ?: NutritionInfo()) }
    var showNutritionEditor by remember { mutableStateOf(false) }
    var nameSuggestions by remember { mutableStateOf<List<LocalFoodItem>>(emptyList()) }
    var showNameSuggestions by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var catExpanded  by remember { mutableStateOf(false) }

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

            // ── Name with autocomplete ────────────────────────────────────────
            Box {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        name = newName
                        nameSuggestions = allFoodItems.filter { food ->
                            food.displayName.contains(newName, ignoreCase = true) ||
                                    food.name.contains(newName, ignoreCase = true)
                        }.take(6)
                        showNameSuggestions = nameSuggestions.isNotEmpty() && newName.isNotBlank()
                    },
                    label = { Text("Item name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = showNameSuggestions,
                    onDismissRequest = { showNameSuggestions = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    // Built-in food names that cannot be deleted
                    val builtInNames = remember {
                        com.martin.storage.data.model.builtInFoodItems.map { it.name }.toSet()
                    }
                    nameSuggestions.forEach { food ->
                        val isUserAdded = food.name !in builtInNames
                        var showDeleteConfirm by remember { mutableStateOf(false) }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(food.displayName, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            food.category + if (food.tags.isNotEmpty()) " · ${food.tags.take(2).joinToString(", ")}" else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                    // Only show delete button for user-added items
                                    if (isUserAdded) {
                                        IconButton(
                                            onClick = { showDeleteConfirm = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove ${food.displayName} from autofill",
                                                tint = Error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                name           = food.displayName
                                category       = food.category
                                unit           = food.defaultUnit
                                amount         = food.defaultAmount.toString()
                                tagsRaw        = food.tags.joinToString(", ")
                                threshold      = food.lowStockThreshold.toString()
                                nutritionState = food.nutrition
                                showNameSuggestions = false
                            }
                        )

                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Remove from Autofill?") },
                                text = { Text("\"${food.displayName}\" will be removed from suggestions. Any existing inventory items using this name are unaffected.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onDeleteFoodItem(food.name)
                                        showDeleteConfirm = false
                                        showNameSuggestions = false
                                    }) { Text("Remove", color = Error) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                                }
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    allCategories.forEach { cat ->
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

            // ── Nutrition editor (expandable) ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerHigh)
                    .clickable { showNutritionEditor = !showNutritionEditor }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Science, null, Modifier.size(16.dp), tint = Tertiary)
                    Text(
                        "Nutrition Info (per 100g / ml)",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface
                    )
                }
                Text(
                    if (showNutritionEditor) "Collapse ▲" else "Edit ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary
                )
            }

            AnimatedVisibility(showNutritionEditor) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Macronutrients", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    NutritionInputRow("Calories", nutritionState.calories, "kcal") { nutritionState = nutritionState.copy(calories = it) }
                    NutritionInputRow("Protein", nutritionState.protein, "g") { nutritionState = nutritionState.copy(protein = it) }
                    NutritionInputRow("Carbohydrates", nutritionState.carbs, "g") { nutritionState = nutritionState.copy(carbs = it) }
                    NutritionInputRow("Fat", nutritionState.fat, "g") { nutritionState = nutritionState.copy(fat = it) }
                    NutritionInputRow("Fibre", nutritionState.fiber, "g") { nutritionState = nutritionState.copy(fiber = it) }
                    NutritionInputRow("Sugar", nutritionState.sugar, "g") { nutritionState = nutritionState.copy(sugar = it) }
                    Spacer(Modifier.height(2.dp))
                    Text("Micronutrients", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    NutritionInputRow("Vitamin C", nutritionState.vitaminC, "mg") { nutritionState = nutritionState.copy(vitaminC = it) }
                    NutritionInputRow("Iron", nutritionState.iron, "mg") { nutritionState = nutritionState.copy(iron = it) }
                    NutritionInputRow("Calcium", nutritionState.calcium, "mg") { nutritionState = nutritionState.copy(calcium = it) }
                    NutritionInputRow("Vitamin D", nutritionState.vitaminD, "mcg") { nutritionState = nutritionState.copy(vitaminD = it) }
                    NutritionInputRow("Vitamin A", nutritionState.vitaminA, "mcg RAE") { nutritionState = nutritionState.copy(vitaminA = it) }
                    NutritionInputRow("Magnesium", nutritionState.magnesium, "mg") { nutritionState = nutritionState.copy(magnesium = it) }
                    NutritionInputRow("Zinc", nutritionState.zinc, "mg") { nutritionState = nutritionState.copy(zinc = it) }
                }
            }

            val isValid = name.isNotBlank()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        val tags = tagsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val trimmedName = name.trim()
                        // Persist to local food dataset so future items & recipes can reference it
                        onUpsertFoodItem(
                            LocalFoodItem(
                                name = trimmedName.lowercase(),
                                displayName = trimmedName,
                                category = category,
                                tags = tags,
                                defaultUnit = unit,
                                defaultAmount = amount.toDoubleOrNull() ?: 1.0,
                                lowStockThreshold = threshold.toDoubleOrNull() ?: 1.0,
                                nutrition = nutritionState
                            )
                        )
                        onSave(
                            GroceryItem(
                                id = item?.id ?: java.util.UUID.randomUUID().toString(),
                                name = trimmedName,
                                amount = amount.toDoubleOrNull() ?: 1.0,
                                unit = unit,
                                category = category,
                                tags = tags,
                                expiryDate = expiryDate.trim(),
                                lowStockThreshold = threshold.toDoubleOrNull() ?: 1.0,
                                notes = notes.trim(),
                                addedDate = item?.addedDate ?: todayDateStr(),
                                nutrition = nutritionState
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

@Composable
private fun NutritionInputRow(
    label: String,
    value: Double,
    unit: String,
    onValueChange: (Double) -> Unit
) {
    var text by remember(value) {
        mutableStateOf(if (value == 0.0) "" else value.toBigDecimal().stripTrailingZeros().toPlainString())
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label ($unit)",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                newText.toDoubleOrNull()?.let { onValueChange(it) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.width(90.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(8.dp)
        )
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