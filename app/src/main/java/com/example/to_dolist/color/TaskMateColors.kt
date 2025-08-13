package com.example.to_dolist.color

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object TaskMateColors {
    val PrimaryPurple = Color(0xFF6366F1)
    val SecondaryPurple = Color(0xFF8B5CF6)
    val AccentPink = Color(0xFFEC4899)
    val BackgroundDark = Color(0xFF0F0F23)
    val SurfaceDark = Color(0xFF1A1A2E)
    val CardDark = Color(0xFF16213E)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB4B4B8)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(BackgroundDark, SurfaceDark)
    )

    val cardGradient = Brush.horizontalGradient(
        colors = listOf(CardDark, SurfaceDark)
    )

    val fabGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryPurple, SecondaryPurple)
    )
}