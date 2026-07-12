package com.jarvis.nchat.core.designsystem


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.jarvis.nchat.routes.designsystem.BackgroundLight
import com.jarvis.nchat.routes.designsystem.ErrorRed
import com.jarvis.nchat.routes.designsystem.AccentPrimary
import com.jarvis.nchat.routes.designsystem.AccentSecondary
import com.jarvis.nchat.routes.designsystem.BackgroundDark
import com.jarvis.nchat.routes.designsystem.SurfaceDark
import com.jarvis.nchat.routes.designsystem.SurfaceDarkElevated
import com.jarvis.nchat.routes.designsystem.TextPrimaryDark
import com.jarvis.nchat.routes.designsystem.SurfaceLight
import com.jarvis.nchat.routes.designsystem.SurfaceLightElevated
import com.jarvis.nchat.routes.designsystem.TextPrimaryLight
import com.jarvis.nchat.routes.designsystem.SurfaceDarkChatBubbleIncoming
import com.jarvis.nchat.routes.designsystem.SurfaceDarkChatBubbleOutgoing
import com.jarvis.nchat.routes.designsystem.DividerDark
import com.jarvis.nchat.routes.designsystem.TextSecondaryDark
import com.jarvis.nchat.routes.designsystem.OnlineGreen
import com.jarvis.nchat.routes.designsystem.SurfaceLightChatBubbleIncoming
import com.jarvis.nchat.routes.designsystem.SurfaceLightChatBubbleOutgoing
import com.jarvis.nchat.routes.designsystem.DividerLight
import com.jarvis.nchat.routes.designsystem.TextSecondaryLight
import com.jarvis.nchat.core.designsystem.ChatAppTypography

private val DarkScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkElevated,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = ErrorRed,
)

private val LightScheme = lightColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceLightElevated,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = ErrorRed,
)

data class ExtendedColors(
    val bubbleIncoming: androidx.compose.ui.graphics.Color,
    val bubbleOutgoing: androidx.compose.ui.graphics.Color,
    val divider: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val online: androidx.compose.ui.graphics.Color,
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
        activity?.window?.let { window ->
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChatAppTypography,
            content = content
        )
    }
}
