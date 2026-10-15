package isel.pdm.droidmentor.chathistory.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.chathistory.ChatSummary
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass

const val CHAT_HISTORY_ITEM_TAG = "chat_history_item"

/**
 * One saved chat in the list: an avatar with its initial, the title, a preview of the
 * last message, when it was updated and how many messages it has, and a delete button.
 *
 * @param chat The chat to show.
 * @param onClick Called when the user taps the chat, to open it.
 * @param onDeleteClick Called when the user taps the delete button.
 */
@Composable
fun ChatHistoryItem(
    chat: ChatSummary,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)
    val title = chat.title ?: stringResource(R.string.new_chat)

    // E.g. "5 min ago" / "há 5 min", in the device's language
    val updated = remember(chat.lastUpdatedMillis) {
        DateUtils.getRelativeTimeSpanString(
            chat.lastUpdatedMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }
    // Zero has its own string: in Portuguese the "one" plural also covers 0
    val count = if (chat.messageCount == 0) {
        stringResource(R.string.chat_history_message_count_zero)
    } else {
        pluralStringResource(
            R.plurals.chat_history_message_count,
            chat.messageCount,
            chat.messageCount,
        )
    }

    Row(
        modifier = modifier
            .glass(c, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
            .testTag(CHAT_HISTORY_ITEM_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.crossGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                (title.firstOrNull { it.isLetterOrDigit() } ?: '?').uppercase(),
                color = c.onCrossGradient,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                color = c.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "$updated · $count",
                style = MaterialTheme.typography.labelSmall,
                color = c.accent,
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.chat_history_delete, title),
                tint = c.textSecondary,
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun ChatHistoryItemPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ChatHistoryItem(
                chat = previewChat,
                onClick = { },
                onDeleteClick = { },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun ChatHistoryItemUntitledLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ChatHistoryItem(
                chat = previewChat.copy(title = null, messageCount = 1),
                onClick = { },
                onDeleteClick = { },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

private val previewChat = ChatSummary(
    id = 1L,
    title = "LazyColumn recomposition com uma lista muito longa de mensagens",
    lastMessage = "Pass a key built from something unique, like the message id.",
    lastUpdatedMillis = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
    messageCount = 4,
)