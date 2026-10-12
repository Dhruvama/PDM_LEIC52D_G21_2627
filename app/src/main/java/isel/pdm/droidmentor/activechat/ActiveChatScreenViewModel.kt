package isel.pdm.droidmentor.activechat

import android.net.Uri

/**
 * Everything the Active Chat screen displays, held by ActiveChatScreenViewModel.
 *
 * It is a data class and not a sealed class because waiting for a reply, being offline
 * and showing an error can all be true at the same time.
 *
 * @property chatTitle Title of the current chat, shown in the top bar. `null` means a new chat
 * without a title; the screen then shows a "New chat" string.
 * @property messages Messages of the chat, in the order they are displayed.
 * @property inputText Text currently typed in the composer.
 * @property attachedImage Image attached in the composer and not yet sent, or `null` if there is none.
 * @property isWaitingForReply `true` while the mentor's reply is pending.
 * @property isOffline `true` when there is no network connection.
 * @property errorMessage Error to show to the user, or `null` if there is none.
 */
data class ActiveChatScreenState(
    val chatTitle: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val attachedImage: Uri? = null,
    val isWaitingForReply: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
) {
    /**
     * Whether the user can send right now: no reply is pending, the device is online,
     * and there is some text or an attached image to send.
     */
    val canSend: Boolean
        get() = !isWaitingForReply && !isOffline && (inputText.isNotBlank() || attachedImage != null)
}