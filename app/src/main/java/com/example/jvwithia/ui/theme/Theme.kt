package com.example.jvwithia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PurpleColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = VioletDeep,
    onPrimaryContainer = OnNight,
    secondary = VioletLight,
    onSecondary = Night,
    secondaryContainer = Plum,
    onSecondaryContainer = OnNight,
    tertiary = Magenta,
    onTertiary = Night,
    tertiaryContainer = Color(0xFF581C87),
    onTertiaryContainer = OnNight,
    background = Night,
    onBackground = OnNight,
    surface = NightPurple,
    onSurface = OnNight,
    surfaceVariant = Plum,
    onSurfaceVariant = OnNightMuted,
    outline = GlassBorder,
    outlineVariant = GlassBorder
)

/** Tema roxo fixo: não usa cores dinâmicas para manter a identidade visual do jogo. */
@Composable
fun JVWithIATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PurpleColorScheme,
        typography = Typography,
        content = content
    )
}
