// Theme.kt
package com.example.waywayapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(

    primary = Lime,
    onPrimary = TextDark,

    secondary = DarkCard,
    onSecondary = CardWhite,

    tertiary = WarningOrange,

    background = AppBg,
    onBackground = TextDark,

    surface = CardWhite,
    onSurface = TextDark,

    error = ErrorRed,
    onError = CardWhite
)

private val DarkColorScheme = darkColorScheme(

    primary = Lime,
    onPrimary = TextDark,

    secondary = CardWhite,
    onSecondary = TextDark,

    tertiary = WarningOrange,

    background = DarkCard,
    onBackground = CardWhite,

    surface = Color(0xFF2B3036),
    onSurface = CardWhite,

    error = ErrorRed,
    onError = CardWhite
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

            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
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
