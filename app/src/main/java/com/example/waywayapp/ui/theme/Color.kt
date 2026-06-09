package com.example.waywayapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Material 3 light roles
val WayWayPrimary = Color(0xFF006C4C)
val WayWayOnPrimary = Color(0xFFFFFFFF)
val WayWayPrimaryContainer = Color(0xFF8DF7C8)
val WayWayOnPrimaryContainer = Color(0xFF002115)

val WayWaySecondary = Color(0xFF4C6358)
val WayWayOnSecondary = Color(0xFFFFFFFF)
val WayWaySecondaryContainer = Color(0xFFCFE9D9)
val WayWayOnSecondaryContainer = Color(0xFF092018)

val WayWayTertiary = Color(0xFF3F6374)
val WayWayOnTertiary = Color(0xFFFFFFFF)
val WayWayTertiaryContainer = Color(0xFFC2E8FC)
val WayWayOnTertiaryContainer = Color(0xFF001F2A)

val WayWayError = Color(0xFFBA1A1A)
val WayWayOnError = Color(0xFFFFFFFF)
val WayWayErrorContainer = Color(0xFFFFDAD6)
val WayWayOnErrorContainer = Color(0xFF410002)

val WayWayBackground = Color(0xFFF6FBF7)
val WayWayOnBackground = Color(0xFF171D1A)
val WayWaySurface = Color(0xFFFBFDF9)
val WayWayOnSurface = Color(0xFF171D1A)
val WayWaySurfaceVariant = Color(0xFFDCE5DD)
val WayWayOnSurfaceVariant = Color(0xFF404943)
val WayWayOutline = Color(0xFF707973)
val WayWayOutlineVariant = Color(0xFFC0C9C1)
val WayWayInverseSurface = Color(0xFF2C322E)
val WayWayInverseOnSurface = Color(0xFFEEF1ED)
val WayWayInversePrimary = Color(0xFF70DAAC)
val WayWayScrim = Color(0xFF000000)

// Material 3 dark roles
val WayWayDarkPrimary = Color(0xFF70DAAC)
val WayWayDarkOnPrimary = Color(0xFF003826)
val WayWayDarkPrimaryContainer = Color(0xFF005139)
val WayWayDarkOnPrimaryContainer = Color(0xFF8DF7C8)

val WayWayDarkSecondary = Color(0xFFB4CCBD)
val WayWayDarkOnSecondary = Color(0xFF21352B)
val WayWayDarkSecondaryContainer = Color(0xFF374B40)
val WayWayDarkOnSecondaryContainer = Color(0xFFCFE9D9)

val WayWayDarkTertiary = Color(0xFFA6CCDF)
val WayWayDarkOnTertiary = Color(0xFF0A3444)
val WayWayDarkTertiaryContainer = Color(0xFF274B5B)
val WayWayDarkOnTertiaryContainer = Color(0xFFC2E8FC)

val WayWayDarkError = Color(0xFFFFB4AB)
val WayWayDarkOnError = Color(0xFF690005)
val WayWayDarkErrorContainer = Color(0xFF93000A)
val WayWayDarkOnErrorContainer = Color(0xFFFFDAD6)

val WayWayDarkBackground = Color(0xFF0F1512)
val WayWayDarkOnBackground = Color(0xFFDDE5DE)
val WayWayDarkSurface = Color(0xFF0F1512)
val WayWayDarkOnSurface = Color(0xFFDDE5DE)
val WayWayDarkSurfaceVariant = Color(0xFF404943)
val WayWayDarkOnSurfaceVariant = Color(0xFFC0C9C1)
val WayWayDarkOutline = Color(0xFF8A938C)
val WayWayDarkOutlineVariant = Color(0xFF404943)
val WayWayDarkInverseSurface = Color(0xFFDDE5DE)
val WayWayDarkInverseOnSurface = Color(0xFF2C322E)
val WayWayDarkInversePrimary = Color(0xFF006C4C)

// Compatibility aliases used by existing screens.
val BgLight = WayWayBackground
val AppBg = WayWayBackground
val SoftWhite = WayWaySurface
val CardWhite = WayWaySurface
val DarkCard = WayWayOnSurface
val BorderGray = WayWayOutlineVariant

val TextDark = WayWayOnSurface
val TextGray = WayWayOnSurfaceVariant
val TextLight = WayWayOutline

val Lime = WayWayPrimary
val LimeDark = Color(0xFF005139)
val LimeSoft = WayWayPrimaryContainer

val ErrorRed = WayWayError
val WarningOrange = Color(0xFFB25F00)
val SuccessGreen = WayWayPrimary

val BadgeRed = Color(0xFFDE3730)
val StarYellow = Color(0xFFFFB300)

val HeaderGradient = Brush.verticalGradient(
    colors = listOf(
        WayWayPrimaryContainer,
        Color(0xFFA8F2D2),
        WayWaySurface
    )
)

val LimeGradient = Brush.linearGradient(
    colors = listOf(
        WayWayPrimary,
        Color(0xFF008960)
    )
)

val DarkGradient = Brush.linearGradient(
    colors = listOf(
        WayWayInverseSurface,
        Color(0xFF1D2420)
    )
)

val OrangeGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFDCC2),
        WarningOrange
    )
)

val PinkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFD8E8),
        Color(0xFFB3266B)
    )
)

val BlueGradient = Brush.linearGradient(
    colors = listOf(
        WayWayTertiaryContainer,
        WayWayTertiary
    )
)

val BikeColor = WayWayPrimaryContainer
val DeliveryColor = WayWayTertiaryContainer
