package com.platnumm.openevo.feathur.doc.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkModeSetting: String = "adapt_device",
  themeSetting: String = "device_wallpaper",
  content: @Composable () -> Unit,
) {
  val darkTheme = when (darkModeSetting) {
    "on" -> true
    "off" -> false
    else -> isSystemInDarkTheme()
  }

  val context = LocalContext.current
  val colorScheme = when {
    themeSetting == "monochrome" -> {
      if (darkTheme) {
        darkColorScheme(
          primary = Color.White,
          onPrimary = Color.Black,
          primaryContainer = Color(0xFF374151),
          onPrimaryContainer = Color.White,
          secondary = Color(0xFF9CA3AF),
          onSecondary = Color.Black,
          background = Color(0xFF111827),
          onBackground = Color.White,
          surface = Color(0xFF1F2937),
          onSurface = Color.White,
          surfaceVariant = Color(0xFF374151),
          onSurfaceVariant = Color(0xFFD1D5DB),
          outline = Color(0xFF4B5563)
        )
      } else {
        lightColorScheme(
          primary = Color.Black,
          onPrimary = Color.White,
          primaryContainer = Color(0xFFE5E7EB),
          onPrimaryContainer = Color.Black,
          secondary = Color(0xFF4B5563),
          onSecondary = Color.White,
          background = Color.White,
          onBackground = Color.Black,
          surface = Color(0xFFF3F4F6),
          onSurface = Color.Black,
          surfaceVariant = Color(0xFFE5E7EB),
          onSurfaceVariant = Color(0xFF374151),
          outline = Color(0xFFD1D5DB)
        )
      }
    }
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
