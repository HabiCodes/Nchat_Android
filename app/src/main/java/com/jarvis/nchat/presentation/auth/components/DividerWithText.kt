package com.jarvis.nchat.presentation.auth.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing

/**
 * A horizontal divider split by centered text — the standard "── OR ──"
 * pattern used to separate form-based auth from social/third-party auth.
 *
 * Built from two weighted [HorizontalDivider]s flanking the text, rather
 * than a single divider with a text overlay — avoids manual gap-cutting via
 * [androidx.compose.foundation.Canvas] for what should stay a simple, static
 * component with no custom drawing logic.
 *
 * @param text the label shown between the divider lines, e.g. "OR".
 * @param modifier applied to the outer [Row].
 */
@Composable
fun DividerWithText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.sm),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
@Preview(name = "DividerWithText — light", showBackground = true)
@Composable
private fun DividerWithTextPreview() {
    ChatAppTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(Spacing.lg)) {
            DividerWithText(text = "OR")
        }
    }
}

@Preview(name = "DividerWithText — dark", showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun DividerWithTextDarkPreview() {
    ChatAppTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(Spacing.lg)) {
            DividerWithText(text = "OR CONTINUE WITH")
        }
    }
}