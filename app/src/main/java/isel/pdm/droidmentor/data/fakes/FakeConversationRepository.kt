package isel.pdm.droidmentor.data.fakes

import isel.pdm.droidmentor.domain.history.ChatSummary
import isel.pdm.droidmentor.domain.history.ConversationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

/** Maximum length of a chat title taken from its first message. */
private const val TITLE_MAX_LENGTH = 30

/**
 * A fake implementation of the [ConversationRepository] interface, keeping the chats in memory.
 * We use it to have fine control over the repository behavior while developing the app.
 *
 * @param initialChats The chats that exist when the repository is created.
 */
class FakeConversationRepository(
    initialChats: List<ChatSummary> = sampleChats,
) : ConversationRepository {

    private val saved = MutableStateFlow(initialChats)

    // TODO Verificar se usamos modelo de concorrencia simplificado
    private val nextId = AtomicLong((initialChats.maxOfOrNull { it.id } ?: 0L) + 1)

    override val chats: Flow<List<ChatSummary>> = saved
        .map { chats -> chats.sortedByDescending { it.lastUpdatedMillis } }
        .onStart { delay(500.milliseconds) } // Simulate reading from the database

    /** Pretends the first message was already saved, so the chat has one message. */
    override suspend fun create(firstMessage: String): Long {
        delay(200.milliseconds) // Simulate writing to the database
        val chat = ChatSummary(
            id = nextId.getAndIncrement(),
            title = firstMessage.trim().take(TITLE_MAX_LENGTH),
            lastMessage = firstMessage,
            lastUpdatedMillis = System.currentTimeMillis(),
            messageCount = 1,
        )
        saved.update { it + chat }
        return chat.id
    }

    override suspend fun delete(id: Long) {
        delay(200.milliseconds) // Simulate writing to the database
        saved.update { chats -> chats.filterNot { it.id == id } }
    }
}

private const val HOUR_MILLIS = 60 * 60 * 1000L

/** Chats shown while there is no database yet, so the Chat History screen has content. */
private val sampleChats: List<ChatSummary>
    get() {
        val now = System.currentTimeMillis()
        return listOf(
            ChatSummary(1L, "O que é uma coroutine?", "Uma coroutine é uma computação suspensa…", now - 1 * HOUR_MILLIS, 4),
            ChatSummary(2L, "ViewModel vs Activity", "O ViewModel sobrevive a mudanças de configuração.", now - 5 * HOUR_MILLIS, 6),
            ChatSummary(3L, "Room com Flow", "Os DAOs podem devolver Flow para observar a tabela.", now - 26 * HOUR_MILLIS, 2),
            ChatSummary(4L, null, "Como uso o LazyColumn?", now - 72 * HOUR_MILLIS, 1),
        )
    }
