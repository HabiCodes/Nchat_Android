package com.jarvis.nchat.presentation.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing

/**
 * A full-screen blocking overlay with a centered spinner, used when an
 * operation should prevent *all* screen interaction — not just one button.
 *
 * For a simple form submit (login, register), prefer [NChatButton]'s own
 * `isLoading` parameter instead — it blocks just that button without
 * dimming the rest of the screen. Reach for this overlay when the whole
 * screen needs to be non-interactive, e.g. OTP auto-verification, or a
 * post-login sync step before navigating away.
 *
 * The overlay consumes all touch input via a no-op [clickable] (rather than
 * simply sitting on top with a higher z-index) so taps genuinely cannot pass
 * through to content underneath while it's visible.
 *
 * @param isVisible whether the overlay is shown.
 * @param modifier applied to the outer animated container.
 * @param message optional text shown below the spinner, e.g. "Verifying...".
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(LoadingOverlayDefaults.AnimationDurationMs)),
        exit = fadeOut(tween(LoadingOverlayDefaults.AnimationDurationMs)),
        modifier = modifier,
    ) {
        val blockInteractionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = LoadingOverlayDefaults.ScrimAlpha))
                .clickable(
                    interactionSource = blockInteractionSource,
                    indication = null,
                    onClick = {}, // absorbs taps; intentionally does nothing
                )
                .semantics { contentDescription = "Loading" },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = LoadingOverlayDefaults.Elevation,
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    if (message != null) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

private object LoadingOverlayDefaults {
    const val AnimationDurationMs: Int = 200
    const val ScrimAlpha: Float = 0.32f
    val Elevation = 3.dp
}

@Preview(name = "LoadingOverlay — with message", showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    ChatAppTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Screen content behind overlay", modifier = Modifier.padding(Spacing.lg))
            LoadingOverlay(isVisible = true, message = "Verifying code…")
        }
    }
}