package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldLight,
    secondary = SapphirePrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = SapphireLight,
    tertiary = FinancialWarning,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    error = FinancialExpense,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldDark,
    secondary = SapphirePrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = SapphireDark,
    tertiary = FinancialWarning,
    onTertiary = Color.Black,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    error = FinancialExpense,
    onError = Color.White
)

@Composable
fun FamilyWalletTheme(
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

// Keep MyApplicationTheme alias so tests/legacy code compile without issue
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FamilyWalletTheme(darkTheme = darkTheme, content = content)
}
