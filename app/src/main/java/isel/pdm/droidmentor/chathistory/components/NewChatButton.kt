package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors

const val NEW_CHAT_BUTTON_TAG = "new_chat_button"

/** "New chat" pill filled with the accent gradient. */
@Composable
fun NewChatButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(c.crossGradient)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 18.dp, end = 22.dp, top = 14.dp, bottom = 14.dp)
            .testTag(NEW_CHAT_BUTTON_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = c.onCrossGradient)
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.chat_history_new_chat),
            color = c.onCrossGradient,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewChatButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        NewChatButton(onClick = { }, modifier = Modifier.padding(16.dp))
    }
}