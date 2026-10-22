package isel.pdm.droidmentor.chathistory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Everything the Chat History screen displays, held by [ChatHistoryScreenViewModel].
 * Each subclass is one situation the screen can be in.
 */
sealed class ChatHistoryScreenState {

    /** The saved chats are being loaded. */
    data object Loading : ChatHistoryScreenState()

    /** There are no saved chats. */
    data object Empty : ChatHistoryScreenState()

    /**
     * There are saved chats.
     *
     * @property chats Every saved chat, most recent first. Never empty.
     * @property searchQuery Text currently typed in the search field.
     */
    data class Loaded(
        val chats: List<ChatSummary>,
        val searchQuery: String = "",
    ) : ChatHistoryScreenState() {

        /** The chats whose title or last message contains [searchQuery] (all of them if it is blank). */
        // TODO pesquisar em todas as mensagens, não só na última?? quando as conversas forem guardadas
        val visibleChats: List<ChatSummary> = searchQuery.trim().let { query ->
            if (query.isEmpty()) chats
            else chats.filter {
                (it.title ?: "").contains(query, ignoreCase = true) ||
                        it.lastMessage.contains(query, ignoreCase = true)
            }
        }
    }
}

/**
 * Holds the state of the Chat History screen.
 *
 * Starts [ChatHistoryScreenState.Loading] and loads the saved chats as soon as it is created.
 * Chats are not persisted yet, so loading always ends in [ChatHistoryScreenState.Empty].
 *
 * Deleting a chat only hides it at first, so it can be undone while the snackbar is showing.
 * It is deleted for good in [confirmDelete].
 */
class ChatHistoryScreenViewModel : ViewModel() {

    /** Every saved chat, most recent first; `null` while they are being loaded. */
    private var chats: List<ChatSummary>? by mutableStateOf(null)

    /** Text currently typed in the search field. */
    private var searchQuery by mutableStateOf("")

    /**
     * The chat just deleted, while it can still be undone (the snackbar is showing).
     * It stays in [chats] but is hidden; it is only removed in [confirmDelete].
     */
    var deletedChat: ChatSummary? by mutableStateOf(null)
        private set

    /** What the screen shows, built from the chats, the search and the pending deletion. */
    val state: ChatHistoryScreenState
        get() {
            val all = chats ?: return ChatHistoryScreenState.Loading
            val shown = all.filter { it != deletedChat }
            return if (shown.isEmpty()) ChatHistoryScreenState.Empty
            else ChatHistoryScreenState.Loaded(shown, searchQuery)
        }

    init {
        loadChats()
    }

    /** Loads the saved chats off the main thread, then shows them. */
    private fun loadChats() {
        viewModelScope.launch {
            chats = withContext(Dispatchers.IO) {
                // TODO carregar as conversas guardadas, fora da main thread???? depende do que usar para ler ROOMS?
                emptyList()
            }
        }
    }

    /** Sets the text typed in the search field to [query]. Only possible when there are chats. */
    fun updateSearch(query: String) {
        if (state !is ChatHistoryScreenState.Loaded) return
        searchQuery = query
    }

    /** Hides [chat] from the list; the deletion can be undone until [confirmDelete]. */
    fun deleteChat(chat: ChatSummary) {
        // Only one deletion can be undone at a time: the previous one becomes final
        confirmDelete()
        deletedChat = chat
    }

    /** Shows the deleted chat again, in the place it never left. */
    fun undoDelete() {
        deletedChat = null
    }

    /** Makes the deletion of [deletedChat] final: it can no longer be undone. */
    fun confirmDelete() {
        val chat = deletedChat ?: return
        // TODO apagar também a conversa guardada (chat.id)
        chats = chats?.minus(chat)
        deletedChat = null
    }

    override fun onCleared() {
        // Leaving the screen while the snackbar is showing also makes the deletion final
        confirmDelete()
    }
}
