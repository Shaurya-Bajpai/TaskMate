package com.example.taskmate.color

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object TaskMateColors {
    // Status Bar Color
    val StatusBarColor = Color(0xFF6366F1)

    // Primary Colors - Modern gradient scheme
    val PrimaryPurple = Color(0xFF6366F1) // Indigo
    val SecondaryPurple = Color(0xFF8B5CF6) // Violet
    val AccentPink = Color(0xFFEC4899) // Pink

    // Background Colors
    val BackgroundDark = Color(0xFF0F172A) // Slate 900
    val SurfaceDark = Color(0xFF1E293B) // Slate 800
    val CardDark = Color(0xFF334155) // Slate 700

    // Text Colors
    val TextPrimary = Color(0xFFF8FAFC) // Slate 50
    val TextSecondary = Color(0xFFCBD5E1) // Slate 300
    val TextTertiary = Color(0xFF94A3B8) // Slate 400

    // Status Colors
    val Success = Color(0xFF10B981) // Emerald 500
    val Warning = Color(0xFFF59E0B) // Amber 500
    val Error = Color(0xFFEF4444) // Red 500
    val Info = Color(0xFF3B82F6) // Blue 500

    // Priority Colors
    val HighPriority = Color(0xFFEF4444) // Red
    val MediumPriority = Color(0xFFF59E0B) // Amber
    val LowPriority = Color(0xFF10B981) // Green

    // Category Colors
    val WorkColor = Color(0xFF6366F1) // Indigo
    val PersonalColor = Color(0xFF8B5CF6) // Violet
    val ShoppingColor = Color(0xFFEC4899) // Pink
    val HealthColor = Color(0xFF10B981) // Emerald
    val EducationColor = Color(0xFF3B82F6) // Blue
    val EntertainmentColor = Color(0xFFF59E0B) // Amber
    val TravelColor = Color(0xFF06B6D4) // Cyan
    val OtherColor = Color(0xFF6B7280) // Gray

    // Gradients
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    )

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryPurple, SecondaryPurple, AccentPink)
    )

    val cardGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF334155).copy(alpha = 0.9f),
            Color(0xFF475569).copy(alpha = 0.8f)
        )
    )

    val successGradient = Brush.horizontalGradient(
        colors = listOf(Success, Color(0xFF059669))
    )

    val warningGradient = Brush.horizontalGradient(
        colors = listOf(Warning, Color(0xFFD97706))
    )

    val errorGradient = Brush.horizontalGradient(
        colors = listOf(Error, Color(0xFFDC2626))
    )
}