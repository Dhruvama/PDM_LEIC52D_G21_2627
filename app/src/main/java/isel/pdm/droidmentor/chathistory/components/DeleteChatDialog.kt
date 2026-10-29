package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.history.ChatSummary
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors

const val DELETE_CHAT_DIALOG_TAG = "delete_chat_dialog"
const val DELETE_CHAT_CONFIRM_TAG = "delete_chat_confirm"

/**
 * Asks the user to confirm deleting the chat with id [chatId], if it is one of [chats].
 * Shows nothing when [chatId] is `null` or that chat is no longer there.
 *
 * @param chatId Id of the chat the user wants to delete; `null` when there is none.
 * @param chats The chats the user can see, where the chat is looked up.
 * @param onConfirm Called with the chat when the user taps "Delete".
 * @param onDismiss Called when the user taps "Cancel" or outside the dialog.
 */
@Composable
fun DeleteChatConfirmation(
    chatId: Long?,
    chats: List<ChatSummary>,
    onConfirm: (ChatSummary) -> Unit,
    onDismiss: () -> Unit,
) {
    val chat = chats.firstOrNull { it.id == chatId } ?: return
    DeleteChatDialog(
        title = chat.title ?: stringResource(R.string.new_chat),
        onConfirm = { onConfirm(chat) },
        onDismiss = onDismiss,
    )
}

/**
 * Asks the user to confirm before the chat called [title] is deleted.
 *
 * @param title The title shown for the chat, as in the list.
 * @param onConfirm Called when the user taps "Delete".
 * @param onDismiss Called when the user taps "Cancel" or outside the dialog.
 */
@Composable
fun DeleteChatDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(DELETE_CHAT_DIALOG_TAG),
        shape = RoundedCornerShape(24.dp),
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        textContentColor = c.textSecondary,
        title = { Text(stringResource(R.string.chat_history_delete_dialog_title)) },
        text = { Text(stringResource(R.string.chat_history_delete_dialog_body, title)) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(DELETE_CHAT_CONFIRM_TAG)) {
                Text(
                    stringResource(R.string.chat_history_delete_dialog_confirm),
                    color = c.accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.chat_history_delete_dialog_cancel), color = c.textSecondary)
            }
        },
    )
}

//==================================== TESTS/PREVIEWS ====================================//

@Composable
private fun PreviewContent(colors: AppColors) {
    DroidMentorTheme(colors = colors) {
        DeleteChatDialog(title = "LazyColumn recomposition", onConfirm = { }, onDismiss = { })
    }
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun DeleteChatDialogDarkPreview() = PreviewContent(DefaultDarkColors)

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun DeleteChatDialogLightPreview() = PreviewContent(DefaultLightColors)