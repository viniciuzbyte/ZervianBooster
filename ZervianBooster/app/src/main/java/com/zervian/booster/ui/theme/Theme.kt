package com.zervian.booster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZervianDarkScheme = darkColorScheme(
    primary = ZervianAccentCyan,
    secondary = ZervianAccentBlue,
    tertiary = ZervianAccentGreen,
    background = ZervianBackground,
    surface = ZervianSurface,
    surfaceVariant = ZervianSurfaceAlt,
    onBackground = ZervianTextPrimary,
    onSurface = ZervianTextPrimary,
    error = ZervianAccentRed,
)

/**
 * Zervian selalu dark mode, ini memang kesengajaan desain untuk app booster
 * (bukan limitasi) - konsisten dengan HUD gaya gaming di semua kondisi cahaya.
 */
@Composable
fun ZervianTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZervianDarkScheme,
        typography = ZervianTypography,
        content = content
    )
}
