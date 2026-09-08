package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LuxuryDarkColorScheme =
  darkColorScheme(
    primary = LuxuryPrimary,
    onPrimary = LuxuryVoidBackground,
    primaryContainer = LuxuryPrimaryContainer,
    onPrimaryContainer = LuxuryTextPrimary,
    secondary = LuxurySecondary,
    onSecondary = LuxuryVoidBackground,
    secondaryContainer = LuxurySecondaryContainer,
    onSecondaryContainer = LuxuryTextPrimary,
    tertiary = LuxuryGold,
    onTertiary = LuxuryVoidBackground,
    tertiaryContainer = LuxuryGoldContainer,
    onTertiaryContainer = LuxuryTextPrimary,
    background = LuxuryVoidBackground,
    onBackground = LuxuryTextPrimary,
    surface = LuxurySurface,
    onSurface = LuxuryTextPrimary,
    surfaceVariant = LuxurySurfaceElevated,
    onSurfaceVariant = LuxuryTextSecondary,
    error = LuxuryWarning,
    errorContainer = LuxuryWarningContainer,
    onError = LuxuryTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LuxuryDarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
