package isel.pdm.droidmentor.domain.history

/**
 * A saved chat, summarised: its title, last message and how many messages it has.
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

/**
 * Whether this chat matches the search [query]: its title or last message contains it,
 * ignoring case. A blank query matches every chat.
 */
// TODO pesquisar em todas as mensagens, não só na última?? quando as conversas forem guardadas
fun ChatSummary.matches(query: String): Boolean {
    val trimmed = query.trim()
    return trimmed.isEmpty() ||
            (title ?: "").contains(trimmed, ignoreCase = true) ||
            lastMessage.contains(trimmed, ignoreCase = true)
}