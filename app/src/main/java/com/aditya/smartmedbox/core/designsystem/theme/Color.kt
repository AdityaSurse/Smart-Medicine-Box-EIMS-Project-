package com.aditya.smartmedbox.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// STITCH "EDITORIAL APOTHECARY" PALETTE TOKENS
// ============================================================================

// Light Theme Tokens ("Bone" Canvas & "Paper" Surface)
val PrimaryLight = Color(0xFF1A1A17) // Deep Carbon Ink
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF1C1C19)
val OnPrimaryContainerLight = Color(0xFF85847F)

val SecondaryLight = Color(0xFF3E683A) // Moss Green
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFBCECB2)
val OnSecondaryContainerLight = Color(0xFF243B21)

val TertiaryLight = Color(0xFFC98A1B) // Medicinal Amber / Mustard
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFDDB2)
val OnTertiaryContainerLight = Color(0xFF624000)

val ErrorLight = Color(0xFFD9432B) // Persimmon / Urgent Alert
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF93000A)

val BackgroundLight = Color(0xFFFEF9F0) // Bone Canvas
val OnBackgroundLight = Color(0xFF1D1C16)
val SurfaceLight = Color(0xFFFBF8F2) // Paper Card
val OnSurfaceLight = Color(0xFF1D1C16)
val SurfaceVariantLight = Color(0xFFE7E2D9)
val OnSurfaceVariantLight = Color(0xFF474741)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8F3EA)
val SurfaceContainerLight = Color(0xFFF2EDE4)
val SurfaceContainerHighLight = Color(0xFFECE8DF)
val SurfaceContainerHighestLight = Color(0xFFE7E2D9)

val OutlineLight = Color(0xFF787770)
val OutlineVariantLight = Color(0xFFDDD5C6) // 1px Precision Hairline Border

// Dark Theme Tokens ("Char" Canvas & "Soot" Surface)
val PrimaryDark = Color(0xFFF3EEE5) // Bone Highlight
val OnPrimaryDark = Color(0xFF151410)
val PrimaryContainerDark = Color(0xFF24221C)
val OnPrimaryContainerDark = Color(0xFFC9C6C1)

val SecondaryDark = Color(0xFFA3D39B) // Light Moss
val OnSecondaryDark = Color(0xFF002203)
val SecondaryContainerDark = Color(0xFF265024)
val OnSecondaryContainerDark = Color(0xFFBFEDB5)

val TertiaryDark = Color(0xFFFFB94D) // Light Amber
val OnTertiaryDark = Color(0xFF291800)
val TertiaryContainerDark = Color(0xFF624000)
val OnTertiaryContainerDark = Color(0xFFFFDDB2)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF151410) // Char Canvas
val OnBackgroundDark = Color(0xFFF3EEE5)
val SurfaceDark = Color(0xFF201E19) // Soot Paper Surface
val OnSurfaceDark = Color(0xFFF3EEE5)
val SurfaceVariantDark = Color(0xFF2D2A23)
val OnSurfaceVariantDark = Color(0xFFA8A296)

val SurfaceContainerLowestDark = Color(0xFF12110D)
val SurfaceContainerLowDark = Color(0xFF1A1914)
val SurfaceContainerDark = Color(0xFF24221C)
val SurfaceContainerHighDark = Color(0xFF2D2A23)
val SurfaceContainerHighestDark = Color(0xFF36332B)

val OutlineDark = Color(0xFF787770)
val OutlineVariantDark = Color(0xFF34312A) // Deep Structural Hairline Border

// Legacy status tokens kept for backward compatibility
val StatusTaken = Color(0xFF3E683A)
val StatusPending = Color(0xFFC98A1B)
val StatusMissed = Color(0xFFD9432B)
val TelemetryHighlight = Color(0xFF3E683A)

// ============================================================================
// SEMANTIC STATUS TOKENS (TAKEN / PENDING / MISSED / ON_TIME)
// ============================================================================

@Immutable
data class StatusColorToken(
    val container: Color,
    val content: Color,
    val border: Color
)

@Immutable
data class SemanticColors(
    val taken: StatusColorToken,
    val pending: StatusColorToken,
    val missed: StatusColorToken,
    val onTime: StatusColorToken
)

val LightSemanticColors = SemanticColors(
    taken = StatusColorToken(
        container = Color(0xFFE2F4DD),
        content = Color(0xFF2B5B28),
        border = Color(0xFFA3D39B)
    ),
    pending = StatusColorToken(
        container = Color(0xFFFFF1D6),
        content = Color(0xFF8B5E00),
        border = Color(0xFFFFDDB2)
    ),
    missed = StatusColorToken(
        container = Color(0xFFFFE7E4),
        content = Color(0xFFB32A15),
        border = Color(0xFFFFDAD6)
    ),
    onTime = StatusColorToken(
        container = Color(0xFFE2F4DD),
        content = Color(0xFF2B5B28),
        border = Color(0xFFA3D39B)
    )
)

val DarkSemanticColors = SemanticColors(
    taken = StatusColorToken(
        container = Color(0xFF1C381A),
        content = Color(0xFFA3D39B),
        border = Color(0xFF2D5E2A)
    ),
    pending = StatusColorToken(
        container = Color(0xFF3D2900),
        content = Color(0xFFFFB94D),
        border = Color(0xFF6B4700)
    ),
    missed = StatusColorToken(
        container = Color(0xFF45100B),
        content = Color(0xFFFF8575),
        border = Color(0xFF751C14)
    ),
    onTime = StatusColorToken(
        container = Color(0xFF1C381A),
        content = Color(0xFFA3D39B),
        border = Color(0xFF2D5E2A)
    )
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }
