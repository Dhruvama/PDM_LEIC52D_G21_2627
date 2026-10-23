package isel.pdm.droidmentor.domain.chat

import java.net.URI

/**
 * Who wrote a [ChatMessage]: the person using the app ([USER]) or the
 * Gemini-backed senior Android mentor ([MENTOR]).
 */
enum class Author { USER, MENTOR }

/**
 * A single message shown in the Active Chat screen.
 *
 * @property id Unique identifier of the message within the chat.
 * @property author Who wrote the message.
 * @property text The message body. May contain Markdown code fences (```), which are
 * rendered later by the message bubble.
 * @property imageUri Optional image attached to the message, or `null` if there is none.
 */
data class ChatMessage(
    val id: Long,
    val author: Author,
    val text: String,
    val imageUri: URI? = null,
)