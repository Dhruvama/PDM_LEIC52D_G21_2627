package isel.pdm.droidmentor.chathistory

/**
 * A saved chat, as shown in the Chat History screen.
 *
 * This is a UI model only; it carries no persistence or serialization concerns.
 *
 * @property id Identifier of the chat, used to open it in the Active Chat screen.
 * @property title Title of the chat. `null` means it has no title yet; the screen then
 * shows the "New chat" string.
 * @property lastMessage Text of the last message, shown as a preview below the title.
 * @property lastUpdatedMillis When the last message was sent, in milliseconds since the epoch.
 * @property messageCount How many messages the chat has.
 */
data class ChatSummary(
    val id: Long,
    val title: String?,
    val lastMessage: String,
    val lastUpdatedMillis: Long,
    val messageCount: Int,
)