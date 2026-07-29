package com.jarvis.nchat.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing

/**
 * NChat's foundational text input — the base every text-entry surface in the
 * app (auth forms, search, chat compose bar variants, settings) is built on.
 *
 * Built directly on [BasicTextField] with a fully custom decoration layer —
 * intentionally not a wrapper around [androidx.compose.material3.OutlinedTextField] —
 * so border, icon tint, placeholder, and supporting-text transitions are
 * fully owned by NChat's design system rather than inherited from Material3
 * defaults that may change between library versions.
 *
 * All colors, spacing, shape and animation timing come from
 * [NChatTextFieldDefaults]; nothing here is a literal value.
 *
 * @param value current text value (state hoisted to the caller).
 * @param onValueChange invoked with the new value on every edit.
 * @param modifier applied to the outermost [Column].
 * @param label optional label rendered above the field; stays visible and
 * simply changes color/emphasis with focus and error state.
 * @param placeholder optional hint text shown inside the field when [value] is empty.
 * @param enabled disables input, dims all colors, and blocks focus when false.
 * @param readOnly allows focus/selection but blocks edits when true.
 * @param isError switches border/label/supporting-text to the error color set.
 * @param supportingText helper or error text rendered below the field.
 * When [isError] is true this is announced to accessibility services as an error.
 * @param leadingIcon optional slot rendered before the input, tinted to match field state.
 * @param trailingIcon optional slot rendered after the input (e.g. a visibility toggle).
 * @param singleLine forces single-line input and collapses [minLines]/[maxLines] to 1.
 * @param minLines minimum visible lines when multi-line.
 * @param maxLines maximum visible lines when multi-line.
 * @param visualTransformation e.g. [androidx.compose.ui.text.input.PasswordVisualTransformation].
 * @param keyboardOptions IME configuration (keyboard type, imeAction, etc).
 * @param keyboardActions callbacks for IME actions (Done, Next, etc).
 * @param interactionSource exposed so callers can observe or simulate focus/press
 * state; a private one is created if not supplied.
 * @param shape corner shape of the input container.
 * @param colors full color set for every field state — see [NChatTextFieldDefaults.colors].
 * @param contentPadding padding *inside* the bordered container, around the text/icons.
 */
@Composable
fun NChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = NChatTextFieldDefaults.Shape,
    colors: NChatTextFieldColors = NChatTextFieldDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = NChatTextFieldDefaults.HorizontalPadding,
        vertical = NChatTextFieldDefaults.VerticalPadding,
    ),
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by actualInteractionSource.collectIsFocusedAsState()

    // ---- state → color resolution (single source of truth for this frame) ----
    val targetBorderColor = when {
        !enabled -> colors.disabledBorderColor
        isError -> colors.errorBorderColor
        isFocused -> colors.focusedBorderColor
        else -> colors.unfocusedBorderColor
    }
    val targetLabelColor = when {
        !enabled -> colors.disabledLabelColor
        isError -> colors.errorLabelColor
        isFocused -> colors.focusedLabelColor
        else -> colors.unfocusedLabelColor
    }
    val targetIconColor = when {
        !enabled -> colors.disabledIconColor
        isError -> colors.errorIconColor
        isFocused -> colors.focusedIconColor
        else -> colors.unfocusedIconColor
    }
    val targetTextColor = if (enabled) colors.textColor else colors.disabledTextColor
    val targetPlaceholderColor =
        if (enabled) colors.placeholderColor else colors.disabledPlaceholderColor
    val targetCursorColor = if (isError) colors.errorCursorColor else colors.cursorColor
    val targetSupportingColor = when {
        !enabled -> colors.disabledSupportingTextColor
        isError -> colors.errorSupportingTextColor
        else -> colors.supportingTextColor
    }

    // ---- animated values — every visual transition goes through here ----
    val animSpec =
        tween<Any>(NChatTextFieldDefaults.AnimationDurationMs) // typed below per-property
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(NChatTextFieldDefaults.AnimationDurationMs),
        label = "nchat_border_color",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) NChatTextFieldDefaults.FocusedBorderWidth else NChatTextFieldDefaults.UnfocusedBorderWidth,
        animationSpec = tween(NChatTextFieldDefaults.AnimationDurationMs),
        label = "nchat_border_width",
    )
    val labelColor by animateColorAsState(
        targetValue = targetLabelColor,
        animationSpec = tween(NChatTextFieldDefaults.AnimationDurationMs),
        label = "nchat_label_color",
    )
    val iconColor by animateColorAsState(
        targetValue = targetIconColor,
        animationSpec = tween(NChatTextFieldDefaults.AnimationDurationMs),
        label = "nchat_icon_color",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor,
            )
            Spacer(Modifier.height(NChatTextFieldDefaults.LabelSpacing))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.containerColor, shape)
                .border(width = borderWidth, color = borderColor, shape = shape)
                .padding(contentPadding)
                .then(
                    if (isError && supportingText != null) {
                        Modifier.semantics { error(supportingText) }
                    } else {
                        Modifier
                    }
                ),
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.let { icon ->
                    CompositionLocalProvider(LocalContentColor provides iconColor) { icon() }
                    Spacer(Modifier.width(NChatTextFieldDefaults.IconContentSpacing))
                }

                NChatTextFieldInputArea(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    readOnly = readOnly,
                    placeholder = placeholder,
                    placeholderColor = targetPlaceholderColor,
                    textColor = targetTextColor,
                    cursorColor = targetCursorColor,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    interactionSource = actualInteractionSource,
                    modifier = Modifier.weight(1f),
                )

                trailingIcon?.let { icon ->
                    Spacer(Modifier.width(NChatTextFieldDefaults.IconContentSpacing))
                    CompositionLocalProvider(LocalContentColor provides iconColor) { icon() }
                }
            }
        }

        AnimatedVisibility(
            visible = supportingText != null,
            enter = fadeIn(tween(NChatTextFieldDefaults.AnimationDurationMs)),
            exit = fadeOut(tween(NChatTextFieldDefaults.AnimationDurationMs)),
        ) {
            Column {
                Spacer(Modifier.height(NChatTextFieldDefaults.SupportingTextSpacing))
                Text(
                    text = supportingText.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = targetSupportingColor,
                )
            }
        }
    }
}
/**
 * The text-input + placeholder region of [NChatTextField], isolated into its
 * own composable so it is never nested inside a `RowScope`/`ColumnScope`
 * lambda — that nesting is what causes Compose's scoped `AnimatedVisibility`
 * overloads (`RowScope.AnimatedVisibility`, `ColumnScope.AnimatedVisibility`)
 * to shadow the plain top-level one and fail to resolve. Keeping this as a
 * standalone function is a correctness requirement here, not just a style choice.
 */
@Composable
private fun NChatTextFieldInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    readOnly: Boolean,
    placeholder: String?,
    placeholderColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    cursorColor: androidx.compose.ui.graphics.Color,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    visualTransformation: VisualTransformation,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = value.isEmpty() && placeholder != null,
            enter = fadeIn(tween(NChatTextFieldDefaults.AnimationDurationMs)),
            exit = fadeOut(tween(NChatTextFieldDefaults.AnimationDurationMs)),
        ) {
            Text(
                text = placeholder.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = placeholderColor,
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = LocalTextStyle.current.copy(
                color = textColor,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(cursorColor),
            decorationBox = { innerTextField -> innerTextField() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@Preview(name = "Light — Empty/Focused", showBackground = true)
@Composable
private fun NChatTextFieldPreview_Light() {
    ChatAppTheme(darkTheme = false) {
        Surface {
            Column(Modifier.padding(Spacing.lg)) {
                NChatTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    placeholder = "you@example.com",
                )
            }
        }
    }
}

@Preview(name = "Dark — Filled", showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun NChatTextFieldPreview_Dark() {
    ChatAppTheme(darkTheme = true) {
        Surface {
            Column(Modifier.padding(Spacing.lg)) {
                NChatTextField(
                    value = "hello@nchat.app",
                    onValueChange = {},
                    label = "Email",
                )
            }
        }
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun NChatTextFieldPreview_Error() {
    ChatAppTheme(darkTheme = false) {
        Surface {
            Column(Modifier.padding(Spacing.lg)) {
                NChatTextField(
                    value = "not-an-email",
                    onValueChange = {},
                    label = "Email",
                    isError = true,
                    supportingText = "Enter a valid email address",
                )
            }
        }
    }
}

@Preview(name = "Disabled", showBackground = true)
@Composable
private fun NChatTextFieldPreview_Disabled() {
    ChatAppTheme(darkTheme = false) {
        Surface {
            Column(Modifier.padding(Spacing.lg)) {
                NChatTextField(
                    value = "Locked value",
                    onValueChange = {},
                    label = "Username",
                    enabled = false,
                )
            }
        }
    }
}

@Preview(name = "Filled with supporting text", showBackground = true)
@Composable
private fun NChatTextFieldPreview_Filled() {
    ChatAppTheme(darkTheme = false) {
        Surface {
            Column(Modifier.padding(Spacing.lg)) {
                NChatTextField(
                    value = "dharun",
                    onValueChange = {},
                    label = "Username",
                    supportingText = "This is visible to other users",
                )
            }
        }
    }
}