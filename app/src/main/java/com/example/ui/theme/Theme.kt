package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZirveColorScheme = darkColorScheme(
    primary = ZirveGold,
    onPrimary = Color(0xFF281500),
    primaryContainer = ZirveAmber,
    onPrimaryContainer = Color.White,
    secondary = ZirveMagentaLight,
    onSecondary = Color.White,
    secondaryContainer = ZirveMagentaDark,
    onSecondaryContainer = ZirveGoldLight,
    tertiary = ZirveEmerald,
    onTertiary = Color.White,
    background = ZirveDeepPurple,
    onBackground = ZirveTextPrimary,
    surface = ZirveCardSurface,
    onSurface = ZirveTextPrimary,
    surfaceVariant = Color(0xFF341859),
    onSurfaceVariant = ZirveTextMuted,
    outline = ZirveGold
)

@Composable
fun ZirveOkeyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZirveColorScheme,
        typography = ZirveTypography,
        content = content
    )
}
