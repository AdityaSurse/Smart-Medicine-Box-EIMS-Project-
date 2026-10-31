package com.aditya.smartmedbox.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val default: Dp = 0.dp,
    val micro: Dp = 2.dp,
    val extraSmall: Dp = 4.dp,
    val indicatorDot: Dp = 6.dp,
    val small: Dp = 8.dp,
    val pulseDot: Dp = 10.dp,
    val gutterMobile: Dp = 12.dp,
    val iconSmall: Dp = 14.dp,
    val medium: Dp = 16.dp,
    val marginMobile: Dp = 16.dp,
    val iconMedium: Dp = 18.dp,
    val iconLarge: Dp = 20.dp,
    val iconExtraLarge: Dp = 22.dp,
    val large: Dp = 24.dp,
    val iconDisplay: Dp = 26.dp,
    val stepNodeSize: Dp = 28.dp,
    val extraLarge: Dp = 32.dp,
    val marginDesktop: Dp = 32.dp,
    val chipHeight: Dp = 32.dp,
    val barColumnWidth: Dp = 36.dp,
    val avatarSize: Dp = 40.dp,
    val controlSize: Dp = 44.dp,
    val huge: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 52.dp,
    val inputHeight: Dp = 52.dp,
    val heroButtonHeight: Dp = 54.dp,
    val cardThumbnailSize: Dp = 56.dp,
    val bottomNavHeight: Dp = 68.dp,
    val barChartHeight: Dp = 80.dp,
    val radarSize: Dp = 112.dp,
    val hairlineBorder: Dp = 1.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
