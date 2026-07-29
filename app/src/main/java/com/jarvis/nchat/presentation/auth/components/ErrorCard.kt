package com.jarvis.nchat.presentation.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing

/**
 * A dedicated surface for presenting error messages — network failures,
 * validation errors, auth failures — anywhere in the app.
 *
 * Wraps its content in [AnimatedVisibility] so callers can pass a nullable
 * message directly (`message = uiState.errorOrNull`) without needing their
 * own show/hide logic; the card animates in and out on its own.
 *
 * The container is marked as a semantics live region, so screen readers
 * announce new error text automatically as it appears — important for
 * async errors (e.g. a failed network call) which don't otherwise have a
 * natural focus point for a screen reader to land on.
 *
 * @param message the error text to display. Pass null to hide the card.
 * @param modifier applied to the outer animated container.
 * @param shape corner shape of the card surface.
 */
@Composable
fun ErrorCard(
    message: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(ErrorCardDefaults.AnimationDurationMs)) +
                expandVertically(tween(ErrorCardDefaults.AnimationDurationMs)),
        exit = fadeOut(tween(ErrorCardDefaults.AnimationDurationMs)) +
                shrinkVertically(tween(ErrorCardDefaults.AnimationDurationMs)),
        modifier = modifier,
    ) {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.errorContainer.takeOrElse(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                },
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = Spacing.md,
                    vertical = Spacing.sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null, // decorative — message text below carries the meaning
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private object ErrorCardDefaults {
    const val AnimationDurationMs: Int = 200
}

/**
 * Fallback for [androidx.compose.material3.ColorScheme.errorContainer] since
 * your current [com.jarvis.nchat.core.designsystem.ChatAppTheme] doesn't set
 * it explicitly — falls back to a low-alpha tint of [MaterialTheme.colorScheme.error]
 * so this still looks intentional without requiring a theme change right now.
 */
@Composable
private fun androidx.compose.ui.graphics.Color.takeOrElse(): androidx.compose.ui.graphics.Color {
    val scheme = MaterialTheme.colorScheme
    return if (this == androidx.compose.ui.graphics.Color.Unspecified) {
        scheme.error.copy(alpha = 0.12f)
    } else {
        this
    }
}

@Preview(name = "ErrorCard — visible/hidden", showBackground = true)
@Composable
private fun ErrorCardPreview() {
    ChatAppTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.md),
        ) {
            ErrorCard(message = "Invalid email or password")
            ErrorCard(message = null) // renders nothing — confirms null-hides-cleanly behavior
        }
    }
}

@Preview(name = "ErrorCard — dark", showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun ErrorCardPreviewDark() {
    ChatAppTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(Spacing.lg)) {
            ErrorCard(message = "Server is waking up, please try again in a moment")
        }
    }
}