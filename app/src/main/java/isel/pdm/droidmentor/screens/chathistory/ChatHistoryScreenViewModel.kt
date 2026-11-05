package isel.pdm.droidmentor.screens.chathistory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.pdm.droidmentor.DependenciesContainer
import isel.pdm.droidmentor.domain.history.ChatSummary
import isel.pdm.droidmentor.domain.history.ConversationRepository
import isel.pdm.droidmentor.domain.history.matches
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

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

        /** The chats that match [searchQuery] (all of them if it is blank). */
        val visibleChats: List<ChatSummary> = chats.filter { it.matches(searchQuery) }
    }
}

/**
 * Holds the state of the Chat History screen.
 *
 * Starts [ChatHistoryScreenState.Loading] and observes the chats in [repository] as soon as
 * it is created, so the list follows every change (including chats created in other screens).
 *
 * Deleting a chat only hides it at first, so it can be undone while the snackbar is showing.
 * It is deleted for good in [confirmDelete].
 */
class ChatHistoryScreenViewModel(
    private val repository: ConversationRepository,
) : ViewModel() {

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

    /** Observes the saved chats: every new list from the repository replaces the shown one. */
    private fun loadChats() {
        viewModelScope.launch {
            repository.chats.collect { chats = it }
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
        // Removed here at once, so it doesn't show up again until the repository emits the list without it
        chats = chats?.minus(chat)
        deletedChat = null
        // NonCancellable: onCleared also calls this, when viewModelScope is already cancelled
        viewModelScope.launch(NonCancellable) { repository.delete(chat.id) }
    }

    override fun onCleared() {
        // Leaving the screen while the snackbar is showing also makes the deletion final
        confirmDelete()
    }

    companion object {
        /** Factory that gives the ViewModel the app's repository. */
        fun factory(dependencies: DependenciesContainer) = viewModelFactory {
            initializer { ChatHistoryScreenViewModel(dependencies.conversationRepository) }
        }
    }
}
