package isel.pdm.droidmentor.activechat

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.activechat.components.ChatComposer
import isel.pdm.droidmentor.activechat.components.ChatTopBar
import isel.pdm.droidmentor.activechat.components.ErrorBanner
import isel.pdm.droidmentor.activechat.components.MessageList
import isel.pdm.droidmentor.activechat.components.OfflineBanner
import isel.pdm.droidmentor.activechat.components.ParticleLogoColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val ACTIVE_CHAT_SCREEN_TAG = "active_chat_screen"

// The real ActiveChatScreen(viewModel, ...) root, with a Scaffold, is added in the ViewModel step.
// This empty stub only exists because ActiveChatActivity already calls ActiveChatScreen().
/**
 * Root of the Active Chat screen. Empty for now; it will get the ViewModel in a later step.
 */
@Composable
fun ActiveChatScreen() {
}

/**
 * Draws the whole Active Chat screen from [state]: top bar, status banners, message list
 * and composer. Stateless: every user action is passed straight to a callback.
 *
 * @param state What the screen shows.
 * @param onBackClick Called when the user taps the back button.
 * @param onOptionsClick Called when the user taps the options button.
 * @param onInputChange Called with the new text whenever the user types.
 * @param onAttachClick Called when the user taps the attach button.
 * @param onCameraClick Called when the user taps the camera button.
 * @param onRemoveAttachment Called when the user removes the attached image.
 * @param onSendClick Called when the user taps the send button.
 * @param onEditMessage Called with the user's message whose "Edit" button was tapped.
 * @param onCopyMessage Called with the mentor's message whose "Copy" button was tapped.
 * @param onRegenerateMessage Called with the mentor's message whose "Regenerate" button was tapped.
 * @param onRetryClick Called when the user taps "Retry" on the error banner.
 * @param onDismissError Called when the user dismisses the error banner.
 * @param assembleLogo `true` when coming from the Title screen, so the welcome's logo
 * gathers from its cloud. The Activity will set it from an Intent extra.
 * @param logoColors Colors of the welcome's logo chosen by the user, or `null` to use the theme.
 * Will come from the user's settings.
 */
@Composable
private fun ActiveChatScreenContent(
    state: ActiveChatScreenState,
    onBackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSendClick: () -> Unit,
    onEditMessage: (ChatMessage) -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    onRetryClick: () -> Unit,
    onDismissError: () -> Unit,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current)
            .testTag(ACTIVE_CHAT_SCREEN_TAG),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                title = state.chatTitle ?: stringResource(R.string.new_chat),
                onBackClick = onBackClick,
                onOptionsClick = onOptionsClick,
            )
            if (state.isOffline) {
                OfflineBanner()
            }
            if (state.errorMessage != null) {
                ErrorBanner(
                    message = state.errorMessage,
                    onRetryClick = onRetryClick,
                    onDismissClick = onDismissError,
                )
            }
            MessageList(
                messages = state.messages,
                isWaitingForReply = state.isWaitingForReply,
                onEditMessage = onEditMessage,
                onCopyMessage = onCopyMessage,
                onRegenerateMessage = onRegenerateMessage,
                onSuggestionClick = onInputChange,
                assembleLogo = assembleLogo,
                logoColors = logoColors,
                modifier = Modifier.weight(1f),
            )
            ChatComposer(
                text = state.inputText,
                onTextChange = onInputChange,
                attachedImage = state.attachedImage,
                onRemoveAttachment = onRemoveAttachment,
                isInputEnabled = !state.isOffline && !state.isWaitingForReply,
                isSendEnabled = state.canSend,
                onAttachClick = onAttachClick,
                onCameraClick = onCameraClick,
                onSendClick = onSendClick,
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
            )
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenNormalPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState,
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenNewChatPreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(
            state = ActiveChatScreenState(),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenNewChatWithCustomLogoColorsPreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(
            state = ActiveChatScreenState(),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
            assembleLogo = true,
            logoColors = ParticleLogoColors(
                main = Color(0xFF22D3EE),      // ciano
                second = Color(0xFFA3E635),    // lima
                highlight = Color.White,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenWaitingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(isWaitingForReply = true),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenOfflinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(isOffline = true),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenErrorPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(errorMessage = previewErrorMessage),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenWithAttachmentPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(
                inputText = previewAttachmentText,
                attachedImage = Uri.EMPTY,
            ),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenLightThemePreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ActiveChatScreenContent(
            state = previewNormalState,
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenVioletThemePreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(
            state = previewNormalState,
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 860, heightDp = 400)
@Composable
private fun ActiveChatScreenLandscapePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState,
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 860, heightDp = 400)
@Composable
private fun ActiveChatScreenNewChatLandscapePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = ActiveChatScreenState(),
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var state by remember { mutableStateOf(previewNormalState) }
        ActiveChatScreenContent(
            state = state,
            onBackClick = { },
            onOptionsClick = { },
            onInputChange = { state = state.copy(inputText = it) },
            onAttachClick = { },
            onCameraClick = { },
            onRemoveAttachment = { },
            onSendClick = { },
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onRetryClick = { },
            onDismissError = { },
        )
    }
}

private const val previewErrorMessage = "Too many requests (429). Try again in a moment."

private const val previewAttachmentText = "What's wrong with this layout?"

private val previewMessages = listOf(
    ChatMessage(
        id = 1L,
        author = Author.USER,
        text = "Why does my LazyColumn recompose every item when I add one message?",
    ),
    ChatMessage(
        id = 2L,
        author = Author.MENTOR,
        text = "Without stable keys, Compose identifies items by position. " +
            "Adding an item shifts the positions, so every item looks new.",
    ),
    ChatMessage(
        id = 3L,
        author = Author.USER,
        text = "How do I give them stable keys?",
    ),
    ChatMessage(
        id = 4L,
        author = Author.MENTOR,
        text = """
            Pass a key built from something unique, like the message id:
            ```kotlin
            LazyColumn {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }
            ```
            Now only the new item is composed.
        """.trimIndent(),
    ),
)

private val previewNormalState = ActiveChatScreenState(
    chatTitle = "LazyColumn recomposition",
    messages = previewMessages,
)