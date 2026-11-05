package isel.pdm.droidmentor.domain.history

import kotlinx.coroutines.flow.Flow

/**
 * Contract for repositories that keep the user's saved chats.
 */
interface ConversationRepository {
    /** Every saved chat, most recent first. Emits again whenever the chats change. */
    val chats: Flow<List<ChatSummary>>

    /**
     * Creates a new chat, titled from [firstMessage].
     * @return The id of the created chat.
     */
    suspend fun create(firstMessage: String): Long

    /**
     * Deletes the chat with the given [id] and all its messages.
     * Does nothing if it does not exist.
     */
    suspend fun delete(id: Long)
}
