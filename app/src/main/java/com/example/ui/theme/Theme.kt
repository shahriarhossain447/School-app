package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = SlateBgDark,
    onBackground = SlateTextDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextDark,
    surfaceVariant = SlateCardDark,
    onSurfaceVariant = SlateMutedDark,
    outline = SlateBorderDark,
    error = ExpenseRed,
    errorContainer = Color(0xFF451A1A),
    onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = AccentPurple,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = SlateBgLight,
    onBackground = SlateTextLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextLight,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = SlateMutedLight,
    outline = SlateBorderLight,
    error = ExpenseRed,
    errorContainer = ExpenseRedContainer,
    onErrorContainer = OnExpenseRedContainer
)

@Composable
fun SchoolFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
