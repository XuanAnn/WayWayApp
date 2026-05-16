// Color.kt
package com.example.waywayapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


val BgLight = Color(0xFFF2FAFF)


val AppBg = Color(0xFFE7E8E2)
val SoftWhite = Color(0xFFF7F8F3)

val CardWhite = Color(0xFFFBFFFC)
val DarkCard = Color(0xFF20242A)

val BorderGray = Color(0xFFE1E3DC)

// ─────────────────────────────────────────────
// TEXT
// ─────────────────────────────────────────────

val TextDark = Color(0xFF20242A)
val TextGray = Color(0xFF8B918A)
val TextLight = Color(0xFFB6BBB3)

// ─────────────────────────────────────────────
// PRIMARY
// ─────────────────────────────────────────────

val Lime = Color(0xFF4DC591)
val LimeDark = Color(0xFF43CE91)
val LimeSoft = Color(0xFFE9FF9A)

// ─────────────────────────────────────────────
// STATUS
// ─────────────────────────────────────────────

val ErrorRed = Color(0xFFFF4D4F)
val WarningOrange = Color(0xFFFFA914)
val SuccessGreen = Color(0xFF2ECC71)

val BadgeRed = Color(0xFFFF3B30)
val StarYellow = Color(0xFFFFB300)

// ─────────────────────────────────────────────
// GRADIENTS
// ─────────────────────────────────────────────

val HeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE9FF74),
        Color(0xFFD8FF4F),
        Color(0xFFCFFF3D)
    )
)

val LimeGradient = Brush.linearGradient(
    colors = listOf(
        Lime,
        LimeDark
    )
)

val DarkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2B3036),
        Color(0xFF171A1F)
    )
)

val OrangeGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFD8A8),
        Color(0xFFFFB347)
    )
)

val PinkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF8CC8),
        Color(0xFFD60087)
    )
)

val BlueGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF7AD7FF),
        Color(0xFF4DA3FF)
    )
)

// ─────────────────────────────────────────────
// SERVICE COLORS
// ─────────────────────────────────────────────

val BikeColor = Color(0xFFE9FF9A)
val FoodColor = Color(0xFFFFDDB8)
val DeliveryColor = Color(0xFF2B3036)

// ─────────────────────────────────────────────
// CARD BACKGROUNDS
// ─────────────────────────────────────────────

val FoodCardPink = Color(0xFFFFDCEB)
val FoodCardYellow = Color(0xFFFFF1BA)
val FoodCardBlue = Color(0xFFBFE9FF)
val FoodCardPurple = Color(0xFFE7E8FF)
val FoodCardGreen = Color(0xFFE9FF9A)
