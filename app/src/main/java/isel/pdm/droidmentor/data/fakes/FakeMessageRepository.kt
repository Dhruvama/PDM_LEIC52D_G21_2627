package isel.pdm.droidmentor.data.fakes

import isel.pdm.droidmentor.domain.chat.Author
import isel.pdm.droidmentor.domain.chat.ChatMessage
import isel.pdm.droidmentor.domain.chat.MessageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of the [MessageRepository] interface, keeping the messages in memory.
 * We use it to have fine control over the repository behavior while developing the app.
 *
 * It is meant to be used only during development and in tests, confined to the main thread. It is
 * therefore not thread safe: each operation is atomic because it runs without suspending. The
 * collection is published through a state flow, so that all observable queries are updated after
 * each change.
 *
 * Message ids are unique across all chats, as they will be in the database.
 *
 * @param initialMessages The messages of each chat when the repository is created, oldest first,
 * by chat id.
 */
class FakeMessageRepository(
    initialMessages: Map<Long, List<ChatMessage>> = emptyMap(),
) : MessageRepository {

    /** The messages of every chat, oldest first, by chat id. Replaced whole on each change. */
    private val saved = MutableStateFlow(initialMessages)

    /** The id given to the next added message: one more than the highest id ever used. */
    private var nextId = (initialMessages.values.flatten().maxOfOrNull { it.id } ?: 0L) + 1

    /**
     * Waits 300 ms before the first emission, to simulate reading from the database.
     * Changes to other chats do not emit again.
     *
     * @param conversationId Id of the chat whose messages are observed.
     * @return A flow with the messages of the chat, oldest first, or an empty list if it has none.
     */
    override fun messages(conversationId: Long): Flow<List<ChatMessage>> = saved
        .map { chats -> chats[conversationId].orEmpty() }
        .distinctUntilChanged() // Changes to other chats don't emit again
        .onStart { delay(300.milliseconds) } // Simulate reading from the database

    /**
     * Waits 100 ms, to simulate writing to the database. Adding to a chat with no messages
     * starts it; the chat is not checked against the [ConversationRepository].
     *
     * @param conversationId Id of the chat the message is added to.
     * @param author Who wrote the message.
     * @param text The message body.
     * @param imageUri Image attached to the message, or `null` if there is none.
     * @return The id of the new message, unique across all chats.
     */
    override suspend fun add(
        conversationId: Long,
        author: Author,
        text: String,
        imageUri: URI?,
    ): Long {
        delay(100.milliseconds) // Simulate writing to the database
        val message = ChatMessage(nextId++, author, text, imageUri)
        val chats = saved.value
        saved.value = chats + (conversationId to chats[conversationId].orEmpty() + message)
        return message.id
    }

    /**
     * Waits 100 ms, to simulate writing to the database. The edit and the deletion are a single
     * update of the state flow, so collectors never see one without the other.
     *
     * @param conversationId Id of the chat to edit.
     * @param messageId Id of the user message to edit.
     * @param newText New text of the message.
     * @return `true` if the message was edited; `false`, with nothing changed, if it is not in
     * that chat or was not written by [Author.USER].
     */
    override suspend fun rewrite(conversationId: Long, messageId: Long, newText: String): Boolean {
        delay(100.milliseconds) // Simulate writing to the database
        val chats = saved.value
        val messages = chats[conversationId].orEmpty()
        val position = messages.indexOfFirst { it.id == messageId }
        if (position == -1 || messages[position].author != Author.USER) return false

        // A single assignment, so the edit and the deletion are seen together, as in a transaction
        val edited = messages[position].copy(text = newText)
        saved.value = chats + (conversationId to messages.take(position) + edited)
        return true
    }

    /**
     * Waits 100 ms, to simulate writing to the database, even when there is nothing to delete.
     * Does nothing if the message is not in that chat.
     *
     * @param conversationId Id of the chat to cut.
     * @param messageId Id of the first message to delete; every message after it is deleted too.
     */
    override suspend fun deleteFrom(conversationId: Long, messageId: Long) {
        delay(100.milliseconds) // Simulate writing to the database
        val chats = saved.value
        val messages = chats[conversationId] ?: return
        val position = messages.indexOfFirst { it.id == messageId }
        if (position == -1) return
        saved.value = chats + (conversationId to messages.take(position))
    }
}
