package isel.pdm.droidmentor.screens.titlescreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/** Test tag of the "Start a chat" button. */
const val START_CHAT_BUTTON_TAG = "start_chat_button"

/**
 * Pill-shaped glass button with an accent halo. The caller decides its width through [modifier].
 *
 * @param onClick Called when the user taps the button.
 */
@Composable
fun StartChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .height(58.dp)
            .frostedGlass(c, CircleShape)
            .glass(c, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag(START_CHAT_BUTTON_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.title_start_chat),
                style = MaterialTheme.typography.titleMedium,
                color = c.accent,
            )
            // Decorative: the button text already says what it does.
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = c.accent,
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun StartChatButtonDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        StartChatButton(
            onClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp).fillMaxWidth(),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun StartChatButtonLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        StartChatButton(
            onClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp).fillMaxWidth(),
        )
    }
}