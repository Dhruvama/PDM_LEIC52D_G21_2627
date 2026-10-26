package isel.pdm.droidmentor.activechat

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import isel.pdm.droidmentor.buildLogTag
import isel.pdm.droidmentor.domain.chat.Author
import isel.pdm.droidmentor.domain.chat.ChatMessage
import isel.pdm.droidmentor.domain.chat.FailureReason
import java.net.URI


/** What the conversation is doing. Mutually exclusive, like PuzzleScreenState in the course example. */
sealed class ChatStatus {
    /** Nothing pending: the user can send a message. */
    data object Idle : ChatStatus()

    /** A message was sent and the mentor's reply is pending. */
    data object Waiting : ChatStatus()

    /** The last request failed for [reason]. */
    data class Failed(val reason: FailureReason) : ChatStatus()
}

/**
 * Everything the Active Chat screen displays, held by ActiveChatScreenViewModel.
 *
 * [status] says what the conversation is doing, and only one of those can happen at a time
 * (idle, waiting for a reply, or failed). [isOffline] is kept apart because it is independent
 * of the status: the network can drop in any of them.
 *
 * @property chatTitle Title of the current chat, shown in the top bar. `null` means a new chat
 * without a title; the screen then shows a "New chat" string.
 * @property messages Messages of the chat, in the order they are displayed.
 * @property inputText Text currently typed in the composer.
 * @property attachedImage Image attached in the composer and not yet sent, or `null` if there is none.
 * @property status What the conversation is doing: idle, waiting for the mentor's reply, or failed.
 * @property isOffline `true` when there is no network connection. Independent of [status]:
 * the network can drop in any status.
 * @property editingMessageId Id of the user message being edited. Messages after it will be
 * replaced when the edit is sent. `null` = not editing. While editing, [inputText] and
 * [attachedImage] hold the edited message's text and image.
 */
data class ActiveChatScreenState(
    val chatTitle: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val attachedImage: Uri? = null,
    val status: ChatStatus = ChatStatus.Idle,
    val isOffline: Boolean = false,
    val editingMessageId: Long? = null,
) {
    /** `true` while the user is editing one of their messages (see [editingMessageId]). */
    val isEditing: Boolean
        get() = editingMessageId != null

    /**
     * Whether the user can send right now: the conversation is idle, the device is online,
     * and there is some text or an attached image to send.
     * The same rule applies when sending an edited message.
     */
    val canSend: Boolean
        get() = status is ChatStatus.Idle && !isOffline && (inputText.isNotBlank() || attachedImage != null)
}

/**
 * ViewModel of the Active Chat screen. Holds the conversation in memory for now:
 * nothing is saved and the mentor does not answer yet.
 */
class ActiveChatScreenViewModel : ViewModel() {

    private val logTag = buildLogTag(this::class.java.simpleName)

    init {
        Log.v(logTag, "init on ${hashCode()}")
    }

    /** ViewModel life confirmation */
    override fun onCleared() {
        super.onCleared()
        Log.v(logTag, "onCleared() on ${hashCode()}")
    }

    /** What the screen shows. Only this ViewModel changes it. */
    var state by mutableStateOf(ActiveChatScreenState())
        private set

    /** For now lives only in memory, TODO (pass this work to the database) */
    private var nextMessageId = 1L

    /**
     * Sets the text typed in the composer to [text].
     * Allowed while waiting for the mentor's reply, so the user can write the next message;
     * only [send] waits for the reply.
     */
    fun updateInput(text: String) {
        state = state.copy(inputText = text)
    }

    /**
     * Attaches the image at [uri] to the message being written, replacing any previous one.
     * Allowed while waiting for the mentor's reply; the image stays in the composer until sent.
     *
     * In a later phase the file is copied to app storage before being kept.
     */
    fun attachImage(uri: Uri) {
        state = state.copy(attachedImage = uri)
    }

    /** Removes the attached image. Ignored if there is none. */
    fun removeAttachedImage() {
        if (state.attachedImage == null) {
            Log.d(logTag, "removeAttachedImage ignored: no image attached")
            return
        }
        state = state.copy(attachedImage = null)
    }

    /**
     * Sends the typed text and the attached image as a new user message, then clears the composer.
     * The first message of a new chat also gives the chat its title.
     * Ignored when [ActiveChatScreenState.canSend] is false.
     */
    fun send() {
        if (!state.canSend) {
            Log.d(logTag, "send ignored: cannot send in the current state")
            return
        }
        val text = state.inputText.trim()
        val message = ChatMessage(
            id = nextMessageId++,
            author = Author.USER,
            text = text,
            imageUri = state.attachedImage?.let { URI.create(it.toString()) },
        )
        state = state.copy(
            messages = state.messages + message,
            inputText = "",
            attachedImage = null,
            chatTitle = state.chatTitle ?: text.take(30),
        )
        // TODO (phase B): save to Room
        // TODO (phase C): call Gemini
    }

    fun startEditing(message_id: Long){
        TODO()
    }

    fun cancelEditing(){
        TODO()
    }

    fun regenerateMessage(message_id: Long){
        TODO()
    }

    fun retry(){
        TODO()
    }

    /** Hides the error banner and returns to [ChatStatus.Idle]. Ignored if there is no error. */
    fun dismissError() {
        if (state.status !is ChatStatus.Failed) {
            Log.d(logTag, "dismissError ignored: no error to dismiss")
            return
        }
        state = state.copy(status = ChatStatus.Idle)
    }
}
