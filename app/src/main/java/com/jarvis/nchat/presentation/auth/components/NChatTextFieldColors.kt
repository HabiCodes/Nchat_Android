package com.jarvis.nchat.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.nchat.core.designsystem.Spacing

/**
 * Centralized default values for [NChatTextField].
 *
 * Every visual token — shape, color-per-state, spacing, border width,
 * animation timing — lives here and nowhere else. A design change (e.g.
 * "make error borders thicker") becomes a one-line edit in this file
 * instead of a project-wide search, and every value is independently
 * previewable and testable.
 */
object NChatTextFieldDefaults {

    /** Corner shape, sourced from the theme's shape scale (14.dp) — stays
     *  visually consistent with cards/sheets/buttons if the scale changes. */
    val Shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes.medium

    /** Border width at rest — hairline, for a quiet, minimal look. */
    val UnfocusedBorderWidth: Dp = 1.dp

    /** Border width while focused. Focus is signaled by weight *and* color,
     *  not color alone — matters for users with color vision deficiencies. */
    val FocusedBorderWidth: Dp = 1.5.dp

    val HorizontalPadding: Dp = Spacing.lg // 16.dp
    val VerticalPadding: Dp = Spacing.md   // 12.dp
    val LabelSpacing: Dp = Spacing.xs      // 4.dp — gap between label and box
    val SupportingTextSpacing: Dp = Spacing.xs
    val IconContentSpacing: Dp = Spacing.sm // 8.dp — gap between icon and text

    /** Border / label / icon / placeholder transition duration. Held inside
     *  the spec's 150–250ms "subtle" band. */
    const val AnimationDurationMs: Int = 200

    /**
     * Full color set for every field state, resolved once per composition
     * from [MaterialTheme.colorScheme] only — never a raw hex literal — so
     * the field re-themes automatically for dark mode or any future brand
     * color change.
     */
    @Composable
    fun colors(): NChatTextFieldColors {
        val scheme = MaterialTheme.colorScheme
        return NChatTextFieldColors(
            focusedBorderColor = scheme.primary,
            unfocusedBorderColor = scheme.outline,
            disabledBorderColor = scheme.outline.copy(alpha = 0.38f),
            errorBorderColor = scheme.error,

            focusedLabelColor = scheme.primary,
            unfocusedLabelColor = scheme.onSurfaceVariant,
            disabledLabelColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            errorLabelColor = scheme.error,

            textColor = scheme.onSurface,
            disabledTextColor = scheme.onSurface.copy(alpha = 0.38f),
            cursorColor = scheme.primary,
            errorCursorColor = scheme.error,

            placeholderColor = scheme.onSurfaceVariant,
            disabledPlaceholderColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),

            unfocusedIconColor = scheme.onSurfaceVariant,
            focusedIconColor = scheme.primary,
            disabledIconColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),
            errorIconColor = scheme.error,

            supportingTextColor = scheme.onSurfaceVariant,
            errorSupportingTextColor = scheme.error,
            disabledSupportingTextColor = scheme.onSurfaceVariant.copy(alpha = 0.38f),

            containerColor = Color.Transparent,
        )
    }
}

/**
 * Fully explicit color set for [NChatTextField]. Deliberately our own type
 * rather than Material3's [androidx.compose.material3.TextFieldColors] —
 * that type is built for M3's internal `DecorationBox` and only exposes
 * private-backed composable accessors, which would silently re-couple our
 * custom decoration layer to M3 internals we're specifically not using here.
 */
@Immutable
data class NChatTextFieldColors(
    val focusedBorderColor: Color,
    val unfocusedBorderColor: Color,
    val disabledBorderColor: Color,
    val errorBorderColor: Color,
    val focusedLabelColor: Color,
    val unfocusedLabelColor: Color,
    val disabledLabelColor: Color,
    val errorLabelColor: Color,
    val textColor: Color,
    val disabledTextColor: Color,
    val cursorColor: Color,
    val errorCursorColor: Color,
    val placeholderColor: Color,
    val disabledPlaceholderColor: Color,
    val unfocusedIconColor: Color,
    val focusedIconColor: Color,
    val disabledIconColor: Color,
    val errorIconColor: Color,
    val supportingTextColor: Color,
    val errorSupportingTextColor: Color,
    val disabledSupportingTextColor: Color,
    val containerColor: Color,
)