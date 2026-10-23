package isel.pdm.droidmentor.activechat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.activechat.Author
import isel.pdm.droidmentor.activechat.ChatMessage
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors

const val USER_EDIT_BUTTON_TAG = "user_edit_button"

/**
 * A message written by the user, aligned to the end of the screen.
 *
 * Shows an image placeholder above the text when the message has an image,
 * and a small edit icon button inside the bubble, after the text.
 *
 * @param message The message to show. Expected to have [Author.USER].
 * @param onEditClick Called when the user taps the edit button.
 * @param modifier Modifier applied to the whole row.
 */
@Composable
fun UserMessageBubbleComponent(
    message: ChatMessage,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomEnd = 6.dp,
        bottomStart = 20.dp,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(color = colors.accentContainer, shape = bubbleShape)
                .border(width = 1.dp, color = colors.border, shape = bubbleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (message.imageUri != null) {
                ComponentImagePlaceholder()
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
                Row(modifier = Modifier.align(Alignment.End)) {
                    ComponentEditButton(onClick = onEditClick)
                }
            }
        }
    }
}

/**
 * Stand-in for the attached image until real image loading is added.
 */
@Composable
private fun ComponentImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AccountBox,
            contentDescription = stringResource(R.string.user_message_attached_image),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Small icon-only edit button shown inside the user's bubble.
 * It has no background of its own, so it sits directly on the bubble color.
 */
@Composable
private fun ComponentEditButton(onClick: () -> Unit) {
    val colors = LocalAppColors.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .testTag(USER_EDIT_BUTTON_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(R.string.user_message_edit_description),
            tint = colors.textSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun UserMessageBubbleComponentTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        UserMessageBubbleComponent(message = previewTextMessage, onEditClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun UserMessageBubbleComponentImagePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        UserMessageBubbleComponent(message = previewImageMessage, onEditClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentImagePlaceholderPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentImagePlaceholder()
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentEditButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentEditButton(onClick = { })
    }
}

private val previewTextMessage = ChatMessage(
    id = 1L,
    author = Author.USER,
    text = "How do I keep state across configuration changes in Compose?",
)

private val previewImageMessage = ChatMessage(
    id = 2L,
    author = Author.USER,
    text = "Why does my layout look like this?",
    imageUri = Uri.EMPTY,
)