package com.martin.storage.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.martin.storage.ui.theme.*
import kotlin.math.roundToInt

// ── Glass Card ─────────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .shadow(elevation, shape, ambientColor = Primary.copy(alpha = 0.12f))
        .clip(shape)
        .background(Color.White.copy(alpha = 0.85f))
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
            fontWeight = FontWeight.Bold
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
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
        IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = "${value.roundedTo1()} $unit",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurface,
            modifier = Modifier.widthIn(min = 56.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = Primary,
                modifier = Modifier.size(16.dp)
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

// ── Helpers ───────────────────────────────────────────────────────────────────

fun Double.roundedTo1(): String {
    val rounded = (this * 10.0).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
    else rounded.toString()
}