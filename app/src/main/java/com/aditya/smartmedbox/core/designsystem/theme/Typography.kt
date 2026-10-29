package com.aditya.smartmedbox.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Stitch Font Stacks: Newsreader (Editorial Serif) & DM Sans (Clear Humanist Sans)
val EditorialSerif = FontFamily.Serif
val HumanistSans = FontFamily.SansSerif

val SmartMedBoxTypography =
    Typography(
        // Newsreader 56px / 60px -0.02em (Editorial authority for countdowns & metrics)
        displayLarge =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 56.sp,
            lineHeight = 60.sp,
            letterSpacing = (-0.02).sp
        ),
        // Newsreader 40px / 44px -0.015em
        displayMedium =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.015).sp
        ),
        // Newsreader 32px / 36px -0.015em
        displaySmall =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.015).sp
        ),
        // Newsreader 28px / 34px -0.01em
        headlineLarge =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.01).sp
        ),
        // Newsreader 22px / 28px
        headlineMedium =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        // Newsreader 18px / 24px
        headlineSmall =
        TextStyle(
            fontFamily = EditorialSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        // DM Sans Title scale
        titleLarge =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        titleMedium =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.01.sp
        ),
        titleSmall =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.01.sp
        ),
        // DM Sans 18px / 26px (Senior-friendly floor for medication review)
        bodyLarge =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        // DM Sans 16px / 24px
        bodyMedium =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        // DM Sans 14px / 20px
        bodySmall =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ),
        // DM Sans 14px / 20px 0.01em
        labelLarge =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.01.sp
        ),
        // DM Sans 12px / 16px
        labelMedium =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.04.sp
        ),
        // DM Sans "label-caps" 11px / 14px with generous 0.08em tracking for compartment/status pips
        labelSmall =
        TextStyle(
            fontFamily = HumanistSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.08.sp
        )
    )
