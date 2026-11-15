package isel.pdm.droidmentor.domain.chat

import isel.pdm.droidmentor.domain.PersistenceException
import kotlinx.coroutines.flow.Flow
import java.net.URI

/**
 * Contract for repositories that keep the messages of each saved chat.
 *
 * Storage failures surface only as [PersistenceException]: thrown by the suspend functions
 * (all of them `@throws PersistenceException`) and emitted as the error of [messages].
 */
interface MessageRepository {

    /**
     * The messages of the chat with the given [conversationId], oldest first.
     * Emits again whenever they change. Emits an empty list if the chat does not exist.
     * Fails with a [PersistenceException] if the messages cannot be read.
     */
    fun messages(conversationId: Long): Flow<List<ChatMessage>>

    /**
     * Saves a new message at the end of the chat with the given [conversationId].
     *
     * @param author Who wrote the message.
     * @param text The message body.
     * @param imageUri Image attached to the message, or `null` if there is none.
     * @return The id of the saved message.
     * @throws PersistenceException If the message cannot be saved.
     */
    suspend fun add(conversationId: Long, author: Author, text: String, imageUri: URI? = null): Long

    /**
     * Replaces the text of the user message with the given [messageId] with [newText] and deletes
     * every message after it in the chat with the given [conversationId], all at once: either
     * both happen or neither does. The message keeps its id, author and image.
     * Used to rewrite the chat from an edited user message.
     *
     * @return `true` if the message was rewritten; `false`, with nothing changed, if it does not
     * exist in that chat or was not written by [Author.USER].
     * @throws PersistenceException If the chat cannot be rewritten; nothing is changed.
     */
    suspend fun rewrite(conversationId: Long, messageId: Long, newText: String): Boolean

    /**
     * Deletes the message with the given [messageId] and every message after it in the chat
     * with the given [conversationId]. Used to regenerate a mentor reply: the reply is deleted
     * and the chat is sent again.
     * Does nothing if the message does not exist in that chat.
     * @throws PersistenceException If the messages cannot be deleted.
     */
    suspend fun deleteFrom(conversationId: Long, messageId: Long)
}
