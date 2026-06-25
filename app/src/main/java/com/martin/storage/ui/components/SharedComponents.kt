package com.martin.storage.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.martin.storage.ui.theme.Error
import com.martin.storage.ui.theme.ErrorContainer
import com.martin.storage.ui.theme.InverseOnSurface
import com.martin.storage.ui.theme.InverseSurface
import com.martin.storage.ui.theme.OnErrorContainer
import com.martin.storage.ui.theme.OnPrimary
import com.martin.storage.ui.theme.OnPrimaryContainer
import com.martin.storage.ui.theme.OnSurface
import com.martin.storage.ui.theme.OnSurfaceVariant
import com.martin.storage.ui.theme.OutlineVariant
import com.martin.storage.ui.theme.Primary
import com.martin.storage.ui.theme.PrimaryContainer
import com.martin.storage.ui.theme.Secondary
import com.martin.storage.ui.theme.SecondaryContainer
import com.martin.storage.ui.theme.Surface
import com.martin.storage.ui.theme.SurfaceContainerHigh
import com.martin.storage.ui.theme.SurfaceContainerHighest
import com.martin.storage.ui.theme.SurfaceContainerLowest
import com.martin.storage.ui.theme.Tertiary
import com.martin.storage.ui.theme.TertiaryContainer
import kotlin.math.roundToInt

// ── Glass Card ─────────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .shadow(elevation, shape, ambientColor = Primary.copy(alpha = 0.12f))
        .clip(shape)
        .background(Color.White)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Column(modifier = baseModifier, content = content)
}

// ── Nutrient Chip ─────────────────────────────────────────────────────────────

@Composable
fun NutrientChip(
    label: String,
    color: Color = PrimaryContainer,
    textColor: Color = OnPrimaryContainer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.20f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Category Chip (selectable) ────────────────────────────────────────────────

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) Primary else SurfaceContainerHigh
    val text = if (selected) OnPrimary else OnSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = text)
    }
}

// ── Progress Bar ──────────────────────────────────────────────────────────────

@Composable
fun NutrientBar(
    label: String,
    current: Double,
    target: Double,
    color: Color = Primary,
    unit: String = "g",
    modifier: Modifier = Modifier
) {
    val pct = if (target > 0) (current / target).coerceIn(0.0, 1.0).toFloat() else 0f
    val animPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "nutrientBar"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurface)
            Text(
                "${current.roundedTo1()}$unit / ${target.roundedTo1()}$unit",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animPct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(color)
            )
        }
    }
}

// ── Circular Progress ─────────────────────────────────────────────────────────

@Composable
fun CircularProgress(
    progress: Float,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = SurfaceContainerHighest,
    progressColor: Color = Primary,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "circularProgress"
    )
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (this.size.minDimension - stroke) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            // Track
            drawCircle(color = trackColor, radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animProgress * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    cap = StrokeCap.Round
                )
            )
        }
        content()
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
        trailingContent?.invoke()
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = OnSurface)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

// ── Snackbar wrapper ──────────────────────────────────────────────────────────

@Composable
fun UndoSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = InverseSurface,
            contentColor = InverseOnSurface,
            actionColor = PrimaryContainer,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalityTopBar(
    title: String,
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        navigationIcon = {
            if (onNavigateUp != null) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface.copy(alpha = 0.92f)
        )
    )
}

// ── Quantity Stepper ──────────────────────────────────────────────────────────

// Find the entire QuantityStepper function and replace with:
@Composable
fun QuantityStepper(
    value: Double,
    unit: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SurfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(30.dp)) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = Primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = "${value.roundedTo1()} $unit",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurface,
            modifier = Modifier.widthIn(min = 44.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(30.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = Primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
// ── Expiry Badge ──────────────────────────────────────────────────────────────

@Composable
fun ExpiryBadge(daysUntilExpiry: Int?, modifier: Modifier = Modifier) {
    if (daysUntilExpiry == null) return
    val (bg, fg, label) = when {
        daysUntilExpiry < 0  -> Triple(ErrorContainer, OnErrorContainer, "Expired")
        daysUntilExpiry == 0 -> Triple(ErrorContainer, OnErrorContainer, "Expires Today")
        daysUntilExpiry <= 3 -> Triple(SecondaryContainer.copy(alpha = 0.5f), Secondary, "Exp. in ${daysUntilExpiry}d")
        else                  -> Triple(TertiaryContainer.copy(alpha = 0.25f), Tertiary, "Exp. in ${daysUntilExpiry}d")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

// ── Stock Status Icon ─────────────────────────────────────────────────────────

@Composable
fun StockIndicator(isLow: Boolean, isOut: Boolean, modifier: Modifier = Modifier) {
    when {
        isOut  -> Box(
            modifier = modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Error)
        )
        isLow  -> Box(
            modifier = modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Secondary)
        )
        else   -> Box(
            modifier = modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Tertiary)
        )
    }
}

// ── Tag-Aware Search Bar ──────────────────────────────────────────────────────

@Composable
fun TagAwareSearchBar(
    textQuery: String,
    onTextQueryChange: (String) -> Unit,
    appliedTagFilters: Set<String>,
    allAvailableTags: List<String>,
    onTagApplied: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    placeholder: String = "Search…",
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSuggestions by remember { mutableStateOf(false) }

    val suggestions = remember(textQuery, allAvailableTags, appliedTagFilters) {
        if (textQuery.isBlank()) emptyList()
        else allAvailableTags
            .filter { it.contains(textQuery.trim(), ignoreCase = true) && it !in appliedTagFilters }
            .sortedBy { it.lowercase().indexOf(textQuery.trim().lowercase()) }
            .take(6)
    }

    LaunchedEffect(suggestions) {
        showSuggestions = suggestions.isNotEmpty()
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = textQuery,
                    onValueChange = {
                        onTextQueryChange(it)
                        if (it.isBlank()) showSuggestions = false
                    },
                    placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                    trailingIcon = {
                        if (textQuery.isNotBlank()) {
                            IconButton(onClick = { onTextQueryChange(""); showSuggestions = false }) {
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
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Autocomplete suggestions card – same width as the text field above
                AnimatedVisibility(
                    visible = showSuggestions,
                    enter = expandVertically(tween(120)) + fadeIn(tween(120)),
                    exit  = shrinkVertically(tween(80)) + fadeOut(tween(80))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        suggestions.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTagApplied(tag)
                                        onTextQueryChange("")
                                        showSuggestions = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "#",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    tag,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }

            trailingContent?.invoke()
        }

        // Applied tag filter chips
        if (appliedTagFilters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filters:",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                appliedTagFilters.forEach { tag ->
                    SearchTagChip(tag = tag, onRemove = { onTagRemoved(tag) })
                }
            }
        }
    }
}

@Composable
fun SearchTagChip(tag: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Primary.copy(alpha = 0.12f))
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.18f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                // We reuse the existing Close icon via import
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $tag filter",
                tint = Primary,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun Double.roundedTo1(): String {
    val rounded = (this * 10.0).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
    else rounded.toString()
}