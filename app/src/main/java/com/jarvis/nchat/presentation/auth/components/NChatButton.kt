package com.jarvis.nchat.presentation.auth.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.Spacing
import androidx.compose.runtime.getValue

/**
 * NChat's primary call-to-action button — used for form submission
 * (login, register, send OTP, etc).
 *
 * Built on [Surface] + manual layout rather than Material3's [androidx.compose.material3.Button],
 * so [contentPadding] is genuinely controllable and the loading state can
 * *replace* the label via [Crossfade] rather than being squeezed alongside it.
 *
 * @param text button label. Ignored visually while [isLoading] is true, but
 * still required for the initial semantics/accessibility label.
 * @param onClick invoked on tap. Not invoked while [enabled] is false or [isLoading] is true.
 * @param modifier applied to the outer [Surface].
 * @param enabled disables interaction and dims colors when false.
 * @param isLoading shows a spinner in place of [text] and blocks further clicks,
 * without changing the button's size (prevents layout jump on submit).
 * @param shape corner shape of the button surface.
 * @param colors full color set for every button state — see [NChatButtonDefaults.colors].
 * @param contentPadding padding around the label/spinner content.
 * @param interactionSource exposed so callers can observe or simulate press state.
 */
@Composable
fun NChatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = NChatButtonDefaults.Shape,
    colors: NChatButtonColors = NChatButtonDefaults.colors(),
    contentPadding: PaddingValues = NChatButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isInteractable = enabled && !isLoading

    val containerColor by animateColorAsState(
        targetValue = if (isInteractable) colors.containerColor else colors.disabledContainerColor,
        animationSpec = tween(NChatButtonDefaults.AnimationDurationMs),
        label = "nchat_button_container_color",
    )
    val contentColor = if (isInteractable) colors.contentColor else colors.disabledContentColor

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(NChatButtonDefaults.Height)
            .semantics { if (!isInteractable) disabled() },
        enabled = isInteractable,
        shape = shape,
        color = containerColor,
        interactionSource = actualInteractionSource,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Crossfade(
                    targetState = isLoading,
                    animationSpec = tween(NChatButtonDefaults.AnimationDurationMs),
                    label = "nchat_button_content",
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(NChatButtonDefaults.SpinnerSize),
                            color = contentColor,
                            strokeWidth = NChatButtonDefaults.SpinnerStrokeWidth,
                        )
                    } else {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

/** Centralized default values for [NChatButton]. Same pattern as [com.jarvis.nchat.core.designsystem.components.NChatTextFieldDefaults]. */
object NChatButtonDefaults {
    val Height: Dp = 52.dp
    val Shape: Shape @Composable get() = MaterialTheme.shapes.medium
    val ContentPadding: PaddingValues = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
    val SpinnerSize: Dp = 20.dp
    val SpinnerStrokeWidth: Dp = 2.dp
    const val AnimationDurationMs: Int = 200

    @Composable
    fun colors(): NChatButtonColors {
        val scheme = MaterialTheme.colorScheme
        return NChatButtonColors(
            containerColor = scheme.primary,
            contentColor = scheme.onPrimary,
            disabledContainerColor = scheme.primary.copy(alpha = 0.38f),
            disabledContentColor = scheme.onPrimary.copy(alpha = 0.38f),
        )
    }
}

@Immutable
data class NChatButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

