package com.example.jvwithia.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta roxa
val Violet = Color(0xFF8B5CF6)
val VioletLight = Color(0xFFC4B5FD)
val VioletDeep = Color(0xFF6D28D9)
val Magenta = Color(0xFFE879F9)
val Pink = Color(0xFFF472B6)

val Night = Color(0xFF0F0720)
val NightPurple = Color(0xFF1E0B3B)
val Plum = Color(0xFF2E1065)

val OnNight = Color(0xFFF5F3FF)
val OnNightMuted = Color(0xFFB9AED6)

// Superfícies translúcidas ("vidro") sobre o fundo em gradiente
val Glass = Color(0x14FFFFFF)
val GlassStrong = Color(0x24FFFFFF)
val GlassBorder = Color(0x26FFFFFF)

// Cores das peças
val XColor = VioletLight
val OColor = Pink

val BackgroundBrush = Brush.verticalGradient(listOf(Plum, NightPurple, Night))
val AccentBrush = Brush.linearGradient(listOf(Violet, Magenta))
