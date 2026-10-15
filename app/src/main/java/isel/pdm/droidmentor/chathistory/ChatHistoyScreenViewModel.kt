package isel.pdm.droidmentor.chathistory

import androidx.lifecycle.ViewModel

/**
 * Everything the Chat History screen displays, held by [ChatHistoryScreenViewModel].
 *
 * @property chats Every saved chat, most recent first.
 * @property searchQuery Text currently typed in the search field.
 * @property isLoading `true` while the saved chats are being loaded.
 */
data class ChatHistoryScreenState(
    val chats: List<ChatSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
) {
    /** The chats whose title or last message contains [searchQuery] (all of them if it is blank). */
    val visibleChats: List<ChatSummary>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return chats
            return chats.filter {
                (it.title ?: "").contains(query, ignoreCase = true) ||
                        it.lastMessage.contains(query, ignoreCase = true)
            }
        }
}

/**
 * Holds the state of the Chat History screen.
 *
 * Not implemented yet: for now it only exists so the screen has where to plug in.
 */
class ChatHistoryScreenViewModel : ViewModel() {
    // TODO expor o estado das conversas guardadas
    // TODO onSearchChange(query): atualizar searchQuery
    // TODO deleteChat(chat): apagar a conversa guardada e atualizar a lista
}