package com.martin.storage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Vitality Flux Color Tokens ────────────────────────────────────────────────
val Primary            = Color(0xFF006B5F)
val OnPrimary          = Color(0xFFFFFFFF)
val PrimaryContainer   = Color(0xFF2DD4BF)
val OnPrimaryContainer = Color(0xFF00574D)

val Secondary            = Color(0xFF944A00)
val OnSecondary          = Color(0xFFFFFFFF)
val SecondaryContainer   = Color(0xFFFD933D)
val OnSecondaryContainer = Color(0xFF693300)

val Tertiary            = Color(0xFF006D36)
val OnTertiary          = Color(0xFFFFFFFF)
val TertiaryContainer   = Color(0xFF41D77A)
val OnTertiaryContainer = Color(0xFF00592A)

val Error            = Color(0xFFBA1A1A)
val OnError          = Color(0xFFFFFFFF)
val ErrorContainer   = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

val Background   = Color(0xFFF7F9FB)
val OnBackground = Color(0xFF191C1E)
val Surface      = Color(0xFFF7F9FB)
val OnSurface    = Color(0xFF191C1E)

val SurfaceVariant    = Color(0xFFE0E3E5)
val OnSurfaceVariant  = Color(0xFF3C4A46)
val SurfaceContainer  = Color(0xFFECEEF0)
val SurfaceContainerHigh    = Color(0xFFE6E8EA)
val SurfaceContainerHighest = Color(0xFFE0E3E5)
val SurfaceContainerLow     = Color(0xFFF2F4F6)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)

val Outline        = Color(0xFF6B7A76)
val OutlineVariant = Color(0xFFBACAC5)
val Scrim          = Color(0xFF000000)

val InverseSurface   = Color(0xFF2D3133)
val InverseOnSurface = Color(0xFFEFF1F3)
val InversePrimary   = Color(0xFF3CDDC7)

// ── Typography ────────────────────────────────────────────────────────────────
val VitalityFluxTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.64).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.44).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)

// ── Color Scheme ──────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                = Primary,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryContainer,
    onPrimaryContainer     = OnPrimaryContainer,
    secondary              = Secondary,
    onSecondary            = OnSecondary,
    secondaryContainer     = SecondaryContainer,
    onSecondaryContainer   = OnSecondaryContainer,
    tertiary               = Tertiary,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,
    error                  = Error,
    onError                = OnError,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,
    background             = Background,
    onBackground           = OnBackground,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    outline                = Outline,
    outlineVariant         = OutlineVariant,
    scrim                  = Scrim,
    inverseSurface         = InverseSurface,
    inverseOnSurface       = InverseOnSurface,
    inversePrimary         = InversePrimary,
    surfaceContainer       = SurfaceContainer,
    surfaceContainerHigh   = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow    = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest
)

@Composable
fun VitalityFluxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = VitalityFluxTypography,
        content     = content
    )
}