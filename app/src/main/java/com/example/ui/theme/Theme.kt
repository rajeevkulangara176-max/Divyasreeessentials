package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BlueSecondary,
    onPrimary = Color.White,
    primaryContainer = Navy800,
    onPrimaryContainer = BlueLight,
    secondary = Slate400,
    onSecondary = Color.Black,
    surface = Navy900,
    onSurface = Slate50,
    background = Color(0xFF070F1E),
    onBackground = Slate50,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = Navy900,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Navy800,
    surface = Color.White,
    onSurface = Navy900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    background = Slate50,
    onBackground = Navy900,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
