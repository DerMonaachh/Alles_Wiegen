package com.example.alleswiegen.ui.theme

import android.app.Activity
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

private val HopDarkColorScheme = darkColorScheme(
    primary = HopPrimary,             // Gold (Buttons & primary actions)
    onPrimary = Color(0xFF1E2226),
    primaryContainer = Color(0xFF705325),
    onPrimaryContainer = Color(0xFFFDECD8),
    secondary = HopSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF234468),
    onSecondaryContainer = Color(0xFFD6E7FC),
    tertiary = HopTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1A462D),
    onTertiaryContainer = Color(0xFFB4F5C9),
    background = HopBackground,       // #525060 (Dark blue-gray background)
    onBackground = Color(0xFFF0F1F3),
    surface = HopSurface,             // #5E5C6C
    onSurface = Color(0xFFF0F1F3),
    surfaceVariant = HopSurfaceVariant, // #6B6979
    onSurfaceVariant = Color(0xFFD5D7DF),
    error = HopError,
    onError = Color.White
)

private val HopLightColorScheme = lightColorScheme(
    primary = HopPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBEAD7),
    onPrimaryContainer = Color(0xFF382307),
    secondary = HopSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E7FC),
    onSecondaryContainer = Color(0xFF0D253F),
    tertiary = HopTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3F9DF),
    onTertiaryContainer = Color(0xFF0B2917),
    background = HopBackground,       // Use #525060 here as well so light system theme doesn't override with white
    onBackground = Color(0xFFF0F1F3),
    surface = HopSurface,
    onSurface = Color(0xFFF0F1F3),
    surfaceVariant = HopSurfaceVariant,
    onSurfaceVariant = Color(0xFFD5D7DF),
    error = HopError,
    onError = Color.White
)

@Composable
fun AllesWiegenTheme(
    darkTheme: Boolean = true, // Force dark theme by default to maintain the hop theme (#525060)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> HopDarkColorScheme
        else -> HopLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
