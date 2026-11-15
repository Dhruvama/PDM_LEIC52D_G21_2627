package isel.pdm.droidmentor.domain.history

import isel.pdm.droidmentor.domain.PersistenceException
import kotlinx.coroutines.flow.Flow

/**
 * Contract for repositories that keep the user's saved chats.
 *
 * Storage failures surface only as [PersistenceException]: thrown by the suspend functions
 * and emitted as the error of [chats].
 */
interface ConversationRepository {
    /**
     * Every saved chat, most recent first. Emits again whenever the chats change.
     * Fails with a [PersistenceException] if the chats cannot be read.
     */
    val chats: Flow<List<ChatSummary>>

    /**
     * Creates a new chat, titled from [firstMessage].
     * @return The id of the created chat.
     * @throws PersistenceException If the chat cannot be saved.
     */
    suspend fun create(firstMessage: String): Long

    /**
     * Deletes the chat with the given [id] and all its messages.
     * Does nothing if it does not exist.
     * @throws PersistenceException If the chat cannot be deleted.
     */
    suspend fun delete(id: Long)
}
