package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = GoldLight,
  onPrimary = NavyDark,
  primaryContainer = NavyContainer,
  onPrimaryContainer = GoldLight,
  secondary = GoldSecondary,
  onSecondary = NavyDark,
  secondaryContainer = NavySurface,
  onSecondaryContainer = GoldLight,
  background = NavyDark,
  onBackground = Color(0xFFF8FAFC),
  surface = NavySurface,
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = NavyContainer,
  onSurfaceVariant = Color(0xFFCBD5E1),
  error = CrimsonError,
  errorContainer = Color(0xFF3B1519),
  onError = Color.White,
  onErrorContainer = Color(0xFFFCA5A5),
  outline = Color(0xFF334A6E),
  outlineVariant = Color(0xFF243650)
)

private val LightColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = Color.White,
  primaryContainer = NavyContainer,
  onPrimaryContainer = Color.White,
  secondary = GoldSecondary,
  onSecondary = NavyDark,
  secondaryContainer = GoldContainer,
  onSecondaryContainer = GoldOnContainer,
  background = CreamBackground,
  onBackground = TextPrimary,
  surface = CreamSurface,
  onSurface = TextPrimary,
  surfaceVariant = CreamSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = CrimsonError,
  errorContainer = CrimsonContainer,
  onError = Color.White,
  onErrorContainer = Color(0xFF7F1D1D),
  outline = BorderSubtle,
  outlineVariant = Color(0xFFE2E8F0)
)

val MaterialTheme.isDarkTheme: Boolean
  @Composable
  @ReadOnlyComposable
  get() = colorScheme.background == NavyDark

val MaterialTheme.adaptiveSurface: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) NavySurface else CreamSurface

val MaterialTheme.adaptiveSurfaceVariant: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) NavyContainer else CreamSurfaceVariant

val MaterialTheme.adaptiveBackground: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) NavyDark else CreamBackground

val MaterialTheme.adaptiveTextPrimary: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) TextDarkPrimary else TextPrimary

val MaterialTheme.adaptiveTextSecondary: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) TextDarkSecondary else TextSecondary

val MaterialTheme.adaptiveTextMuted: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) TextDarkMuted else TextMuted

val MaterialTheme.adaptiveBorder: Color
  @Composable
  @ReadOnlyComposable
  get() = if (colorScheme.background == NavyDark) BorderDarkSubtle else BorderSubtle

/**
 * Standard high-contrast, crystal clear text field colors for all inputs in the app.
 * Guaranteed readability in both Dark and Light modes.
 */
@Composable
fun maitreTextFieldColors(): TextFieldColors {
  val isDark = MaterialTheme.colorScheme.background == NavyDark
  return OutlinedTextFieldDefaults.colors(
    focusedTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
    unfocusedTextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
    disabledTextColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
    focusedContainerColor = if (isDark) Color(0xFF162540) else Color(0xFFFFFFFF),
    unfocusedContainerColor = if (isDark) Color(0xFF0E1A2D) else Color(0xFFFFFFFF),
    disabledContainerColor = if (isDark) Color(0xFF0A1424) else Color(0xFFF1F5F9),
    focusedBorderColor = if (isDark) GoldLight else NavyPrimary,
    unfocusedBorderColor = if (isDark) Color(0xFF334A6E) else Color(0xFFCBD5E1),
    disabledBorderColor = if (isDark) Color(0xFF1E2E44) else Color(0xFFE2E8F0),
    focusedLabelColor = if (isDark) GoldLight else NavyPrimary,
    unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
    disabledLabelColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
    focusedPlaceholderColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
    unfocusedPlaceholderColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
    focusedLeadingIconColor = if (isDark) GoldLight else NavyPrimary,
    unfocusedLeadingIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
    focusedTrailingIconColor = if (isDark) GoldLight else NavyPrimary,
    unfocusedTrailingIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
    cursorColor = if (isDark) GoldLight else NavyPrimary,
    errorBorderColor = CrimsonError,
    errorTextColor = CrimsonError,
    errorLabelColor = CrimsonError,
    errorContainerColor = if (isDark) Color(0xFF2E151A) else CrimsonContainer
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

