package com.jarvis.nchat.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimaryDark,
    errorContainer = ErrorRed.copy(alpha = 0.12f),
    onErrorContainer = ErrorRed,
    secondary = AccentSecondary,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    outlineVariant = DividerDark,
    error = ErrorRed,
    onError = TextPrimaryDark,
)

private val LightScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimaryDark, // near-white reads fine on AccentPrimary in both themes
    secondary = AccentSecondary,
    onSecondary = BackgroundDark,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    outlineVariant = DividerLight,
    error = ErrorRed,
    onError = TextPrimaryDark,
    errorContainer = ErrorRed.copy(alpha = 0.12f),
    onErrorContainer = ErrorRed,
)

/**
 * Colors used across NChat that have no equivalent slot in Material3's
 * [androidx.compose.material3.ColorScheme] (chat bubbles, presence indicator, etc).
 * Marked [Immutable] so Compose can skip recomposition for readers of
 * [ChatAppTheme.extendedColors] when nothing has actually changed.
 */
@Immutable
data class ExtendedColors(
    val bubbleIncoming: Color,
    val bubbleOutgoing: Color,
    val divider: Color,
    val textSecondary: Color,
    val online: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        bubbleIncoming = SurfaceDarkChatBubbleIncoming,
        bubbleOutgoing = SurfaceDarkChatBubbleOutgoing,
        divider = DividerDark,
        textSecondary = TextSecondaryDark,
        online = OnlineGreen,
    )
}

object ChatAppTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}

@Composable
fun ChatAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extended = if (darkTheme) {
        ExtendedColors(SurfaceDarkChatBubbleIncoming, SurfaceDarkChatBubbleOutgoing, DividerDark, TextSecondaryDark, OnlineGreen)
    } else {
        ExtendedColors(SurfaceLightChatBubbleIncoming, SurfaceLightChatBubbleOutgoing, DividerLight, TextSecondaryLight, OnlineGreen)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? android.app.Activity
        // SideEffect ensures this runs only after a successful composition,
        // not on every recomposition — window/system calls are effects, not
        // pure UI output, so they don't belong directly in the composable body.
        SideEffect {
            activity?.window?.let { window ->
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChatAppTypography,
            shapes = ChatAppShapes, // was missing — every MaterialTheme.shapes.* call was silently using M3 defaults
            content = content
        )
    }
}