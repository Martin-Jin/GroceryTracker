package com.martin.storage.ui.inventory

import androidx.lifecycle.*
import com.martin.storage.data.model.*
import com.martin.storage.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class InventoryEvent {
    data class DuplicateName(val name: String) : InventoryEvent()
}
data class InventoryUiState(
    val items: List<GroceryItem>        = emptyList(),
    val allFoodItems: List<LocalFoodItem>       = emptyList(),
    val searchQuery: String             = "",
    val selectedCategory: String?       = null,
    val appliedTagFilters: Set<String>  = emptySet(),
    val sortOrder: SortOrder            = SortOrder.NAME_ASC,
    val isLoading: Boolean              = true,
    val showShoppingListOnly: Boolean   = false,
    val customCategories: List<String>  = emptyList(),
    val canUndo: Boolean                = false,
    val canRedo: Boolean                = false,
) {
    val allCategories: List<String>
        get() = groceryCategories + customCategories

    val filtered: List<GroceryItem>
        get() {
            var list = items
            if (showShoppingListOnly) list = list.filter { it.isLowStock || it.isOutOfStock }
            if (searchQuery.isNotBlank()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (selectedCategory != null) list = list.filter { it.category == selectedCategory }
            if (appliedTagFilters.isNotEmpty()) list = list.filter { item ->
                appliedTagFilters.all { filterTag ->
                    item.tags.any { it.equals(filterTag, ignoreCase = true) }
                }
            }
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
    private val _events = MutableSharedFlow<InventoryEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<InventoryEvent> = _events.asSharedFlow()

    private val undoRedo = UndoRedoManager<List<GroceryItem>>()

    init {
        viewModelScope.launch {
            repo.groceryItems.collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
        // Keep allFoodItems reactive: re-merge whenever the user saves a custom item
        viewModelScope.launch {
            repo.allFoodItems.collect { merged ->
                _state.update { it.copy(allFoodItems = merged) }
            }
        }
    }
    // ── Mutations ─────────────────────────────────────────────────────────────

    fun upsertItem(item: GroceryItem) = viewModelScope.launch {
        val isDuplicate = _state.value.items.any {
            it.name.equals(item.name.trim(), ignoreCase = true) && it.id != item.id
        }
        if (isDuplicate) {
            _events.emit(InventoryEvent.DuplicateName(item.name.trim()))
            return@launch
        }
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

    fun upsertLocalFoodItem(item: LocalFoodItem) = viewModelScope.launch {
        repo.upsertLocalFoodItem(item)
    }

    // ── Filters / Search ─────────────────────────────────────────────────────

    fun setSearch(q: String)            = _state.update { it.copy(searchQuery = q) }
    fun setCategory(cat: String?)       = _state.update { it.copy(selectedCategory = cat) }
    fun clearFilters()                  = _state.update {
        it.copy(searchQuery = "", selectedCategory = null, appliedTagFilters = emptySet(), showShoppingListOnly = false)
    }
    fun setSortOrder(order: SortOrder)  = _state.update { it.copy(sortOrder = order) }
    fun toggleShoppingListOnly()        = _state.update { it.copy(showShoppingListOnly = !it.showShoppingListOnly) }

    // ── Tag filters ───────────────────────────────────────────────────────────

    fun applyTagFilter(tag: String)  = _state.update { it.copy(appliedTagFilters = it.appliedTagFilters + tag) }
    fun removeTagFilter(tag: String) = _state.update { it.copy(appliedTagFilters = it.appliedTagFilters - tag) }

    // ── Category management ───────────────────────────────────────────────────

    fun addCategory(name: String)          = viewModelScope.launch { repo.addCustomCategory(name) }
    fun removeCustomCategory(name: String) = viewModelScope.launch { repo.removeCustomCategory(name) }

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