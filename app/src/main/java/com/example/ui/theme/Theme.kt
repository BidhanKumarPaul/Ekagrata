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

private val DarkColorScheme = darkColorScheme(
    primary = SoftSkyBlue,
    onPrimary = DeepObsidian,
    primaryContainer = CardSurface,
    onPrimaryContainer = SoftSkyBlue,
    secondary = AcademicIndigo,
    onSecondary = DeepObsidian,
    secondaryContainer = CardSurface,
    onSecondaryContainer = AcademicIndigo,
    tertiary = CalmEmerald,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = TextPrimaryDark,
    surface = SlateNavy,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorder,
    error = FocusCrimson
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = IndigoLight,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = IndigoLight,
    tertiary = CalmEmerald,
    onTertiary = Color.White,
    background = WarmPaper,
    onBackground = DeepInk,
    surface = LightSurface,
    onSurface = DeepInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = LightBorder,
    error = FocusCrimson
)

@Composable
fun EkagrataTheme(
    darkTheme: Boolean = true, // Default to deep obsidian navy for focus atmosphere
    dynamicColor: Boolean = false, // Keep tailored brand palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for compatibility with test templates
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    EkagrataTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
