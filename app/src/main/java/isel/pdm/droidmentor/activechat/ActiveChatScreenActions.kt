package isel.pdm.droidmentor.activechat

import isel.pdm.droidmentor.domain.chat.ChatMessage

/**
 * Everything the user can do on the Active Chat screen.
 *
 * Grouped in one interface so [ActiveChatScreenContent] and its previews don't repeat
 * every callback. The screen root ([ActiveChatScreen]) creates one implementation that
 * forwards each action to the ViewModel, to navigation or to the clipboard.
 *
 * Actions:
 * - [onBack]: the user tapped the back button.
 * - [onHistory]: the user tapped the history button.
 * - [onSettings]: the user tapped the options button.
 * - [onInputChange]: the user typed in the input field (or tapped a suggestion).
 * - [onAttach]: the user tapped the attachment button.
 * - [onCamera]: the user tapped the camera button.
 * - [onRemoveAttachment]: the user removed the attached image.
 * - [onSend]: the user tapped the send button.
 * - [onEditMessage]: the user tapped "Edit" on one of their messages.
 * - [onCancelEdit]: the user cancelled editing a message.
 * - [onCopyMessage]: the user tapped "Copy" on a mentor's message.
 * - [onRegenerateMessage]: the user tapped "Regenerate" on a mentor's message.
 * - [onRetry]: the user tapped "Retry" on the error banner.
 * - [onDismissError]: the user dismissed the error banner.
 */
interface ActiveChatActions {
    fun onTitle()
    fun onHistory()
    fun onSettings()
    fun onInputChange(text: String)
    fun onAttach()
    fun onCamera()
    fun onRemoveAttachment()
    fun onSend()
    fun onEditMessage(message: ChatMessage)
    fun onCancelEdit()
    fun onCopyMessage(message: ChatMessage)
    fun onRegenerateMessage(message: ChatMessage)
    fun onRetry()
    fun onDismissError()
}
