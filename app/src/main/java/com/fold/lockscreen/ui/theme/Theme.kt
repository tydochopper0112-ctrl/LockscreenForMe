package com.fold.lockscreen.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = ElectricBlue,
    tertiary = NeonPurple,
    background = PureBlack,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = PureBlack,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = MutedText
)

@Composable
fun LockscreenTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            runCatching {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
