package com.martin.storage.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martin.storage.data.repository.AppRepository
import com.martin.storage.ui.components.GlassCard
import com.martin.storage.ui.components.VitalityTopBar
import com.martin.storage.ui.theme.OnPrimary
import com.martin.storage.ui.theme.OnSurface
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.OutlineVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerHigh
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import com.martin.storage.ui.theme.VitalityFluxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: AppRepository,
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            VitalityTopBar(
                title = "Settings",
                onNavigateUp = onNavigateUp
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            AnimatedVisibility(state.savedFeedback) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TertiaryContainer.copy(.3f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Tertiary, modifier = Modifier.size(18.dp))
                    Text("Settings saved", style = MaterialTheme.typography.labelMedium, color = Tertiary)
                }
            }

            // Daily Calorie Goal
            SettingsSection(
                title = "Daily Calorie Goal",
                icon = Icons.Outlined.LocalFireDepartment,
                iconTint = Secondary
            ) {
                SingleValueRow(
                    label = "Calories",
                    value = state.settings.dailyCalories,
                    unit = "kcal",
                    onSave = viewModel::setDailyCalories
                )
            }

            // Macronutrient Targets
            SettingsSection(
                title = "Macronutrient Targets",
                icon = Icons.Outlined.BarChart,
                iconTint = Primary
            ) {
                SingleValueRow("Protein", state.settings.dailyProtein, "g", viewModel::setDailyProtein)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Carbohydrates", state.settings.dailyCarbs, "g", viewModel::setDailyCarbs)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Fat", state.settings.dailyFat, "g", viewModel::setDailyFat)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Fibre", state.settings.dailyFiber, "g", viewModel::setDailyFiber)
            }

            // Micronutrient Targets
            SettingsSection(
                title = "Micronutrient Targets",
                icon = Icons.Outlined.Science,
                iconTint = Tertiary
            ) {
                SingleValueRow("Vitamin C", state.settings.dailyVitaminC, "mg", viewModel::setDailyVitaminC)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Iron", state.settings.dailyIron, "mg", viewModel::setDailyIron)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Calcium", state.settings.dailyCalcium, "mg", viewModel::setDailyCalcium)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Vitamin D", state.settings.dailyVitaminD, "mcg", viewModel::setDailyVitaminD)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Magnesium", state.settings.dailyMagnesium, "mg", viewModel::setDailyMagnesium)
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SingleValueRow("Zinc", state.settings.dailyZinc, "mg", viewModel::setDailyZinc)
            }

            // Notifications
            SettingsSection(
                title = "Notifications",
                icon = Icons.Outlined.Notifications,
                iconTint = Primary
            ) {
                SwitchRow(
                    label = "Low stock alerts",
                    subtitle = "Notify when items are running low",
                    checked = state.settings.lowStockNotificationsEnabled,
                    onCheckedChange = viewModel::setLowStockNotifications
                )
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SwitchRow(
                    label = "Expiry alerts",
                    subtitle = "Notify before items expire",
                    checked = state.settings.expiryNotificationsEnabled,
                    onCheckedChange = viewModel::setExpiryNotifications
                )
                AnimatedVisibility(state.settings.expiryNotificationsEnabled) {
                    Column {
                        HorizontalDivider(color = OutlineVariant.copy(.4f))
                        SingleIntRow(
                            label = "Expiry warning days",
                            value = state.settings.expiryWarningDays,
                            unit = "days before",
                            onSave = viewModel::setExpiryWarningDays,
                            min = 1,
                            max = 14
                        )
                    }
                }
            }

            // Shopping List
            SettingsSection(
                title = "Shopping List",
                icon = Icons.Outlined.ShoppingCart,
                iconTint = Secondary
            ) {
                SingleValueRow(
                    label = "Default low-stock threshold",
                    value = state.settings.defaultLowStockThreshold,
                    unit = "units",
                    onSave = viewModel::setDefaultLowStockThreshold
                )
            }

            // About
            SettingsSection(
                title = "About",
                icon = Icons.Outlined.Info,
                iconTint = OnSurfaceVariant
            ) {
                AboutRow("App Version", "1.0.0")
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                AboutRow("Data storage", "Local only (DataStore)")
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                AboutRow("Nutrition data", "Built-in food database (per 100g)")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Section container ─────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(4.dp), content = content)
        }
    }
}

// ── Editable value row ────────────────────────────────────────────────────────

@Composable
private fun SingleValueRow(
    label: String,
    value: Double,
    unit: String,
    onSave: (Double) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value.toString().trimEnd('0').trimEnd('.').ifEmpty { "0" }) }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface, modifier = Modifier.weight(1f))
        if (editing) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.width(90.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Text(unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                IconButton(
                    onClick = {
                        text.toDoubleOrNull()?.let { onSave(it) }
                        editing = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Check, "Save", tint = Primary, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { editing = true }
            ) {
                Text("$value $unit", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Primary)
                Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = OnSurfaceVariant)
            }
        }
    }
}

// ── Stepper row for integer values ────────────────────────────────────────────

@Composable
private fun SingleIntRow(
    label: String,
    value: Int,
    unit: String,
    onSave: (Int) -> Unit,
    min: Int = 0,
    max: Int = 100
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { if (value > min) onSave(value - 1) },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = SurfaceContainerHigh)
            ) { Icon(Icons.Default.Remove, null, Modifier.size(16.dp)) }
            Text("$value $unit", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Primary)
            FilledTonalIconButton(
                onClick = { if (value < max) onSave(value + 1) },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = SurfaceContainerHigh)
            ) { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) }
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────

@Composable
private fun SwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = Primary
            )
        )
    }
}

// ── Read-only about row ───────────────────────────────────────────────────────

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
    }
}

@Preview(showBackground = true, name = "Settings Screen")
@Composable
private fun SettingsPreview() {
    VitalityFluxTheme {
        Column(
            Modifier.fillMaxSize().background(Surface).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
            SettingsSection("Daily Calorie Goal", Icons.Outlined.LocalFireDepartment, Secondary) {
                SingleValueRow("Calories", 2000.0, "kcal") {}
            }
            SettingsSection("Notifications", Icons.Outlined.Notifications, Primary) {
                SwitchRow("Low stock alerts", "Notify when items are running low", true) {}
                HorizontalDivider(color = OutlineVariant.copy(.4f))
                SwitchRow("Expiry alerts", "Notify before items expire", false) {}
            }
        }
    }
}