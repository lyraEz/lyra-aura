package com.lyra.aura.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppTheme { LAVENDER_DARK, AMOLED, LAVENDER_LIGHT, SYSTEM }

private val LavenderDarkColorScheme = darkColorScheme(
    primary          = LavenderPrimary,
    onPrimary        = LavenderOnPrimary,
    primaryContainer = Color(0xFF3B0F8C),
    onPrimaryContainer = LavenderAccent,
    secondary        = LavenderSecondary,
    onSecondary      = LavenderOnPrimary,
    secondaryContainer = Color(0xFF2D1B6B),
    onSecondaryContainer = LavenderTertiary,
    tertiary         = LavenderTertiary,
    onTertiary       = Color(0xFF1A0F3C),
    tertiaryContainer = Color(0xFF4C2F99),
    background       = LavenderBg,
    onBackground     = LavenderOnSurface,
    surface          = LavenderSurface,
    onSurface        = LavenderOnSurface,
    surfaceVariant   = LavenderSurface2,
    onSurfaceVariant = LavenderOnSurface2,
    outline          = Color(0xFF5A4D7A),
    outlineVariant   = Color(0xFF3D3057),
    error            = LyraError,
    onError          = Color(0xFF1A0010),
    surfaceTint      = LavenderPrimary,
    inverseSurface   = LavenderAccent,
    inverseOnSurface = LavenderOnPrimary,
    inversePrimary   = LavenderSecondary,
    scrim            = Color(0xFF000000),
)

private val AmoledColorScheme = LavenderDarkColorScheme.copy(
    background = AmoledBg,
    surface    = AmoledSurface,
    surfaceVariant = Color(0xFF0D0D16),
)

private val LavenderLightColorScheme = lightColorScheme(
    primary          = LavenderLightPrimary,
    onPrimary        = Color.White,
    primaryContainer = LavenderLightSurface,
    onPrimaryContainer = Color(0xFF1A0F3C),
    secondary        = LavenderSecondary,
    onSecondary      = Color.White,
    background       = LavenderLightBg,
    onBackground     = Color(0xFF1A0F3C),
    surface          = LavenderLightSurface,
    onSurface        = Color(0xFF1A0F3C),
    surfaceVariant   = Color(0xFFE9E3FF),
    onSurfaceVariant = Color(0xFF4A3E6A),
    outline          = Color(0xFF9D8FC9),
)

data class LyraColors(
    val glassTint: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val statusOnline: Color = StatusOnline,
    val statusIdle: Color = StatusIdle,
    val statusDnd: Color = StatusDnd,
    val statusInvisible: Color = StatusInvisible,
    val cardSurface: Color,
    val shimmer: Color,
)

val LocalLyraColors = staticCompositionLocalOf {
    LyraColors(
        glassTint      = GlassTint.copy(alpha = 0.08f),
        glassBorder    = GlassBorder.copy(alpha = 0.25f),
        glassHighlight = GlassHighlight.copy(alpha = 0.15f),
        cardSurface    = LavenderSurface,
        shimmer        = LavenderPrimary.copy(alpha = 0.3f),
    )
}

@Composable
fun LyraAuraTheme(
    appTheme: AppTheme = AppTheme.LAVENDER_DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.LAVENDER_LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        else -> true
    }

    val colorScheme = when (appTheme) {
        AppTheme.LAVENDER_DARK  -> LavenderDarkColorScheme
        AppTheme.AMOLED         -> AmoledColorScheme
        AppTheme.LAVENDER_LIGHT -> LavenderLightColorScheme
        AppTheme.SYSTEM         -> if (isDark) LavenderDarkColorScheme else LavenderLightColorScheme
    }

    val lyraColors = if (isDark) {
        LyraColors(
            glassTint      = GlassTint.copy(alpha = 0.09f),
            glassBorder    = GlassBorder.copy(alpha = 0.22f),
            glassHighlight = GlassHighlight.copy(alpha = 0.12f),
            cardSurface    = if (appTheme == AppTheme.AMOLED) AmoledSurface else LavenderSurface,
            shimmer        = LavenderPrimary.copy(alpha = 0.35f),
        )
    } else {
        LyraColors(
            glassTint      = LavenderLightPrimary.copy(alpha = 0.07f),
            glassBorder    = LavenderLightPrimary.copy(alpha = 0.2f),
            glassHighlight = Color.White.copy(alpha = 0.6f),
            cardSurface    = LavenderLightSurface,
            shimmer        = LavenderLightPrimary.copy(alpha = 0.2f),
        )
    }

    CompositionLocalProvider(LocalLyraColors provides lyraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = LyraTypography,
            content     = content,
        )
    }
}
