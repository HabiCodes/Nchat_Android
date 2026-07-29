package com.jarvis.nchat.presentation.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

/**
 * Standardized title + optional subtitle block for the top of an auth screen
 * (Login, Register, Forgot Password, OTP, etc).
 *
 * Centralizing this ensures every auth screen shares identical title
 * typography, weight, and title-to-subtitle spacing by construction —
 * rather than each screen re-declaring its own `Text(style = headlineMedium,
 * fontWeight = Bold)` and slowly drifting out of sync.
 *
 * @param title the screen's main heading, e.g. "Welcome back".
 * @param modifier applied to the outer [Column].
 * @param subtitle optional supporting line below the title, e.g.
 * "Log in to continue chatting". Omit for screens that don't need one.
 * @param textAlign alignment for both title and subtitle. Defaults to [TextAlign.Start]
 * to match the left-aligned form fields below it; pass [TextAlign.Center] for
 * a centered layout style if the screen calls for it.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    textAlign: TextAlign = TextAlign.Start,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        if (subtitle != null) {
            Spacer(androidx.compose.ui.Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "ScreenHeader — title only", showBackground = true)
@Composable
private fun ScreenHeaderPreview() {
    ChatAppTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(Spacing.lg)) {
            ScreenHeader(title = "Welcome back")
        }
    }
}

@Preview(name = "ScreenHeader — title + subtitle", showBackground = true)
@Composable
private fun ScreenHeaderWithSubtitlePreview() {
    ChatAppTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(Spacing.lg)) {
            ScreenHeader(
                title = "Create account",
                subtitle = "Join NChat and start messaging",
            )
        }
    }
}

@Preview(name = "ScreenHeader — dark, centered", showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun ScreenHeaderDarkCenteredPreview() {
    ChatAppTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(Spacing.lg)) {
            ScreenHeader(
                title = "Verify OTP",
                subtitle = "Enter the code sent to your phone",
                textAlign = TextAlign.Center,
            )
        }
    }
}