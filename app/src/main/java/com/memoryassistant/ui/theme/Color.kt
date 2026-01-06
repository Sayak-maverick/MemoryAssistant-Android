package com.memoryassistant.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color definitions for Memory Assistant app
 *
 * These are the main colors we'll use throughout the app.
 * Following Material Design 3 color system.
 *
 * Color values are in hexadecimal (0xFF + RGB values)
 * For example: 0xFF2196F3
 *   - 0xFF = fully opaque (not transparent)
 *   - 21 = Red value
 *   - 96 = Green value
 *   - F3 = Blue value
 */

// Light Theme Colors
val Primary = Color(0xFF2196F3)        // Blue - trust, reliability
val Secondary = Color(0xFF4CAF50)      // Green - success, found items
val Tertiary = Color(0xFFFF9800)       // Orange - highlights, important actions

// Dark Theme Colors (darker versions for dark mode)
val PrimaryDark = Color(0xFF1976D2)
val SecondaryDark = Color(0xFF388E3C)
val TertiaryDark = Color(0xFFF57C00)

// Error color (for warnings and errors)
val Error = Color(0xFFF44336)          // Red - alerts, missing items

// Background colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

// Surface colors (for cards, dialogs, etc.)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
