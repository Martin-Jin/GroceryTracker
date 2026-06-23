package com.martin.storage.ui.inventory

import androidx.lifecycle.*
import com.martin.storage.data.model.*
import com.martin.storage.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<GroceryItem>        = emptyList(),
    val searchQuery: String             = "",
    val selectedCategory: String?       = null,
    val selectedTags: Set<String>       = emptySet(),
    val sortOrder: SortOrder            = SortOrder.NAME_ASC,
    val isLoading: Boolean              = true,
    val showShoppingListOnly: Boolean   = false,
    val canUndo: Boolean                = false,
    val canRedo: Boolean                = false
) {
    val filtered: List<GroceryItem>
        get() {
            var list = items
            if (showShoppingListOnly) list = list.filter { it.isLowStock || it.isOutOfStock }
            if (!searchQuery.isBlank()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (selectedCategory != null) list = list.filter { it.category == selectedCategory }
            if (selectedTags.isNotEmpty()) list = list.filter { item -> item.tags.any { it in selectedTags } }
            return when (sortOrder) {
                SortOrder.NAME_ASC    -> list.sortedBy { it.name.lowercase() }
                SortOrder.NAME_DESC   -> list.sortedByDescending { it.name.lowercase() }
                SortOrder.EXPIRY_ASC  -> list.sortedWith(compareBy(nullsLast()) { it.daysUntilExpiry })
                SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
                SortOrder.CATEGORY    -> list.sortedBy { it.category }
            }
        }

    val allTags: List<String>
        get() = items.flatMap { it.tags }.distinct().sorted()

    val lowStockCount: Int
        get() = items.count { it.isLowStock || it.isOutOfStock }

    val expiringCount: Int
        get() = items.count { it.isExpiringSoon || it.isExpired }
}

enum class SortOrder(val label: String) {
    NAME_ASC("Name (A–Z)"), NAME_DESC("Name (Z–A)"),
    EXPIRY_ASC("Expiry (Soonest)"), AMOUNT_DESC("Amount (Most)"),
    CATEGORY("Category")
}

class InventoryViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(InventoryUiState())
    val state: StateFlow<InventoryUiState> = _state.asStateFlow()

    private val undoRedo = UndoRedoManager<List<GroceryItem>>()

    init {
        viewModelScope.launch {
            repo.groceryItems.collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    fun upsertItem(item: GroceryItem) = viewModelScope.launch {
        pushUndo()
        repo.upsertGroceryItem(item)
    }

    fun deleteItem(id: String) = viewModelScope.launch {
        pushUndo()
        repo.deleteGroceryItem(id)
    }

    fun adjustAmount(id: String, delta: Double) = viewModelScope.launch {
        pushUndo()
        repo.adjustAmount(id, delta)
    }

    // ── Filters / Search ─────────────────────────────────────────────────────

    fun setSearch(q: String)            = _state.update { it.copy(searchQuery = q) }
    fun setCategory(cat: String?)       = _state.update { it.copy(selectedCategory = cat) }
    fun toggleTag(tag: String)          = _state.update {
        val tags = if (tag in it.selectedTags) it.selectedTags - tag else it.selectedTags + tag
        it.copy(selectedTags = tags)
    }
    fun clearFilters()                  = _state.update { it.copy(searchQuery = "", selectedCategory = null, selectedTags = emptySet(), showShoppingListOnly = false) }
    fun setSortOrder(order: SortOrder)  = _state.update { it.copy(sortOrder = order) }
    fun toggleShoppingListOnly()        = _state.update { it.copy(showShoppingListOnly = !it.showShoppingListOnly) }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    private fun pushUndo() {
        undoRedo.push(_state.value.items)
        _state.update { it.copy(canUndo = undoRedo.canUndo, canRedo = undoRedo.canRedo) }
    }

    fun undo() = viewModelScope.launch {
        undoRedo.undo(_state.value.items)?.let { previous ->
            repo.saveGroceryItems(previous)
            _state.update { it.copy(canUndo = undoRedo.canUndo, canRedo = undoRedo.canRedo) }
        }
    }

    fun redo() = viewModelScope.launch {
        undoRedo.redo(_state.value.items)?.let { next ->
            repo.saveGroceryItems(next)
            _state.update { it.copy(canUndo = undoRedo.canUndo, canRedo = undoRedo.canRedo) }
        }
    }
}

class InventoryViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = InventoryViewModel(repo) as T
}