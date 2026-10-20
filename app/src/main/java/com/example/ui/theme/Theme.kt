package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudyFluidDarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryCyan,
    onSecondary = LiquidBackground,
    secondaryContainer = SecondaryContainerCyan,
    onSecondaryContainer = TextPrimary,
    tertiary = TertiaryViolet,
    background = LiquidBackground,
    onBackground = TextPrimary,
    surface = LiquidSurface,
    onSurface = TextPrimary,
    surfaceVariant = LiquidSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = LiquidCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // StudyFluid defaults to deep immersive liquid dark theme
    content: @Composable () -> Unit,
) {
    val colorScheme = StudyFluidDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = LiquidBackground.toArgb()
                window.navigationBarColor = LiquidBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
