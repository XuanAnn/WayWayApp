package com.example.waywayapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = WayWayPrimary,
    onPrimary = WayWayOnPrimary,
    primaryContainer = WayWayPrimaryContainer,
    onPrimaryContainer = WayWayOnPrimaryContainer,
    inversePrimary = WayWayInversePrimary,
    secondary = WayWaySecondary,
    onSecondary = WayWayOnSecondary,
    secondaryContainer = WayWaySecondaryContainer,
    onSecondaryContainer = WayWayOnSecondaryContainer,
    tertiary = WayWayTertiary,
    onTertiary = WayWayOnTertiary,
    tertiaryContainer = WayWayTertiaryContainer,
    onTertiaryContainer = WayWayOnTertiaryContainer,
    background = WayWayBackground,
    onBackground = WayWayOnBackground,
    surface = WayWaySurface,
    onSurface = WayWayOnSurface,
    surfaceVariant = WayWaySurfaceVariant,
    onSurfaceVariant = WayWayOnSurfaceVariant,
    inverseSurface = WayWayInverseSurface,
    inverseOnSurface = WayWayInverseOnSurface,
    outline = WayWayOutline,
    outlineVariant = WayWayOutlineVariant,
    scrim = WayWayScrim,
    error = WayWayError,
    onError = WayWayOnError,
    errorContainer = WayWayErrorContainer,
    onErrorContainer = WayWayOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = WayWayDarkPrimary,
    onPrimary = WayWayDarkOnPrimary,
    primaryContainer = WayWayDarkPrimaryContainer,
    onPrimaryContainer = WayWayDarkOnPrimaryContainer,
    inversePrimary = WayWayDarkInversePrimary,
    secondary = WayWayDarkSecondary,
    onSecondary = WayWayDarkOnSecondary,
    secondaryContainer = WayWayDarkSecondaryContainer,
    onSecondaryContainer = WayWayDarkOnSecondaryContainer,
    tertiary = WayWayDarkTertiary,
    onTertiary = WayWayDarkOnTertiary,
    tertiaryContainer = WayWayDarkTertiaryContainer,
    onTertiaryContainer = WayWayDarkOnTertiaryContainer,
    background = WayWayDarkBackground,
    onBackground = WayWayDarkOnBackground,
    surface = WayWayDarkSurface,
    onSurface = WayWayDarkOnSurface,
    surfaceVariant = WayWayDarkSurfaceVariant,
    onSurfaceVariant = WayWayDarkOnSurfaceVariant,
    inverseSurface = WayWayDarkInverseSurface,
    inverseOnSurface = WayWayDarkInverseOnSurface,
    outline = WayWayDarkOutline,
    outlineVariant = WayWayDarkOutlineVariant,
    scrim = WayWayScrim,
    error = WayWayDarkError,
    onError = WayWayDarkOnError,
    errorContainer = WayWayDarkErrorContainer,
    onErrorContainer = WayWayDarkOnErrorContainer
)

@Composable
fun WayWayAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
