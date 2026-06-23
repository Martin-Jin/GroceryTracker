package com.martin.storage.ui.settings

import androidx.lifecycle.*
import com.martin.storage.data.model.UserSettings
import com.martin.storage.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = true,
    val savedFeedback: Boolean = false
)

class SettingsViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.userSettings.collect { settings ->
                _state.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    fun updateSettings(updated: UserSettings) = viewModelScope.launch {
        repo.saveUserSettings(updated)
        _state.update { it.copy(savedFeedback = true) }
        kotlinx.coroutines.delay(1500)
        _state.update { it.copy(savedFeedback = false) }
    }

    // Convenience updaters so each field change doesn't require a full copy at the call site.
    fun setDailyCalories(v: Double)   = save { it.copy(dailyCalories = v) }
    fun setDailyProtein(v: Double)    = save { it.copy(dailyProtein = v) }
    fun setDailyCarbs(v: Double)      = save { it.copy(dailyCarbs = v) }
    fun setDailyFat(v: Double)        = save { it.copy(dailyFat = v) }
    fun setDailyFiber(v: Double)      = save { it.copy(dailyFiber = v) }
    fun setDailyVitaminC(v: Double)   = save { it.copy(dailyVitaminC = v) }
    fun setDailyIron(v: Double)       = save { it.copy(dailyIron = v) }
    fun setDailyCalcium(v: Double)    = save { it.copy(dailyCalcium = v) }
    fun setDailyVitaminD(v: Double)   = save { it.copy(dailyVitaminD = v) }
    fun setDailyMagnesium(v: Double)  = save { it.copy(dailyMagnesium = v) }
    fun setDailyZinc(v: Double)       = save { it.copy(dailyZinc = v) }
    fun setLowStockNotifications(v: Boolean) = save { it.copy(lowStockNotificationsEnabled = v) }
    fun setExpiryNotifications(v: Boolean)   = save { it.copy(expiryNotificationsEnabled = v) }
    fun setExpiryWarningDays(v: Int)         = save { it.copy(expiryWarningDays = v) }
    fun setDefaultLowStockThreshold(v: Double) = save { it.copy(defaultLowStockThreshold = v) }

    private fun save(transform: (UserSettings) -> UserSettings) = viewModelScope.launch {
        val updated = transform(_state.value.settings)
        repo.saveUserSettings(updated)
    }
}

class SettingsViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SettingsViewModel(repo) as T
}