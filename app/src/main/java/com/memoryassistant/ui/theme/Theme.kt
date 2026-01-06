package com.memoryassistant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Color schemes for light and dark modes
 *
 * Material Design 3 uses "color schemes" which define all the colors
 * your app will use in different situations (backgrounds, buttons, text, etc.)
 */

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = Error
)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = Error
)

/**
 * MemoryAssistantTheme - The main theme wrapper for our app
 *
 * This function wraps all our UI and applies the theme (colors, typography, shapes).
 * Think of it like a CSS stylesheet that applies to everything inside it.
 *
 * @param darkTheme - Should we use dark mode? By default, follows system setting
 * @param dynamicColor - Use device's dynamic colors (Android 12+)?
 * @param content - The UI content to wrap with this theme
 */
@Composable
fun MemoryAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Follow system dark mode setting
    dynamicColor: Boolean = true,  // Use system colors on Android 12+
    content: @Composable () -> Unit
) {
    // Choose the right color scheme based on dark mode and device capabilities
    val colorScheme = when {
        // Dynamic colors are available on Android 12+ (API 31+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        // Use our custom dark theme
        darkTheme -> DarkColorScheme

        // Use our custom light theme
        else -> LightColorScheme
    }

    // This adjusts the system bars (status bar, navigation bar) to match our theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Apply the theme to all content inside
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,  // Text styles (we'll keep default for now)
        content = content  // Your app's UI goes here
    )
}
