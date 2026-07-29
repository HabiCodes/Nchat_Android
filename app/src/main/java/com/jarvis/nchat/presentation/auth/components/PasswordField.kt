package com.jarvis.nchat.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.R

/**
 * A password input field built on top of [NChatTextField].
 *
 * Adds exactly two things beyond the base field: a show/hide visibility
 * toggle, and the correct [KeyboardType.Password] + [VisualTransformation]
 * wiring. All colors, spacing, shape, border/label/icon animation, and error
 * handling are inherited unchanged from [NChatTextField] — this composable
 * intentionally does not re-implement any of that.
 *
 * The visibility state is [rememberSaveable] rather than [remember] so it
 * survives configuration changes (rotation, process death from backgrounding)
 * without resetting the user's toggle choice mid-input.
 *
 * @param value current password text (state hoisted to the caller).
 * @param onValueChange invoked with the new value on every edit.
 * @param modifier applied to the outer field.
 * @param label label rendered above the field, e.g. "Password".
 * @param placeholder optional hint text.
 * @param enabled disables input and dims all colors when false.
 * @param isError switches the field to its error color set.
 * @param supportingText helper or error text rendered below the field.
 * @param imeAction IME action for the keyboard (e.g. [androidx.compose.ui.text.input.ImeAction.Done]
 * for the last field in a form, [androidx.compose.ui.text.input.ImeAction.Next] otherwise).
 * @param keyboardActions callbacks for IME actions.
 * @param interactionSource exposed so callers can observe or simulate focus state.
 * @param shape corner shape of the input container.
 * @param colors full color set for every field state.
 * @param contentPadding padding inside the bordered container.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = stringResource(R.string.nchat_password_label),
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = NChatTextFieldDefaults.Shape,
    colors: NChatTextFieldColors = NChatTextFieldDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = NChatTextFieldDefaults.HorizontalPadding,
        vertical = NChatTextFieldDefaults.VerticalPadding,
    ),
) {
    var isVisible by rememberSaveable { mutableStateOf(false) }

    NChatTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        trailingIcon = {
            val descriptionRes = if (isVisible) {
                R.string.nchat_hide_password
            } else {
                R.string.nchat_show_password
            }
            IconButton(
                onClick = { isVisible = !isVisible },
                enabled = enabled,
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(descriptionRes),
                )
            }
        },
    )
}