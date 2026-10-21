package isel.pdm.droidmentor.activechat

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.buildLogTag
import isel.pdm.droidmentor.activechat.components.ChatInputComponent
import isel.pdm.droidmentor.activechat.components.ChatTopBarComponent
import isel.pdm.droidmentor.activechat.components.ErrorBannerComponent
import isel.pdm.droidmentor.activechat.components.MessageListComponent
import isel.pdm.droidmentor.activechat.components.OfflineBannerComponent
import isel.pdm.droidmentor.ui.components.animations.particlelogo.ParticleLogoColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val ACTIVE_CHAT_SCREEN_TAG = "active_chat_screen"

private val logTag = buildLogTag("ActiveChatScreen")

/**
 * Root of the Active Chat screen: connects [viewModel] to the stateless screen content.
 *
 * @param viewModel Holds the conversation and handles the user's actions.
 * @param assembleLogo `true` when coming from the Title screen, so the welcome's logo
 * gathers from its cloud.
 * @param onBack Called when the user taps the back button.
 */
@Composable
fun ActiveChatScreen(viewModel: ActiveChatScreenViewModel, assembleLogo: Boolean, onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    // The content handles the insets and draws its own background.
    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ActiveChatScreenContent(
                state = viewModel.state,
                assembleLogo = assembleLogo,
                onBackClick = onBack,
                onOptionsClick = {
                    Log.v(logTag, "Options clicked")
                    // TODO: options menu
                },
                onInputChange = { viewModel.updateInput(it) },
                onAttachClick = {
                    Log.v(logTag, "Attach clicked")
                    // TODO (phase E): pick an image from the gallery
                },
                onCameraClick = {
                    Log.v(logTag, "Camera clicked")
                    // TODO (phase E): take a photo
                },
                onRemoveAttachment = { viewModel.removeAttachedImage() },
                onSendClick = { viewModel.send() },
                onEditMessage = {
                    Log.v(logTag, "Edit message ${it.id} clicked")
                    // TODO (phase D): edit the message
                },
                onCopyMessage = { clipboard.setText(AnnotatedString(it.text)) },
                onRegenerateMessage = {
                    Log.v(logTag, "Regenerate message ${it.id} clicked")
                    // TODO (phase D): regenerate the reply
                },
                onRetryClick = {
                    Log.v(logTag, "Retry clicked")
                    // TODO (phase C): retry the request to Gemini
                },
                onDismissError = { viewModel.dismissError() },
            )
        }
    }
}

/**
 * Draws the whole Active Chat screen from [state]: top bar, status banners, message list
 * and composer. Stateless: every user action is passed straight to a callback.
 *
 * @param state What the screen shows.
 * @param onBackClick Called when the user taps the back button.
 * @param onOptionsClick Called when the user taps the options button.
 * @param onInputChange Called with the new text whenever the user types.
 * @param onAttachClick Called when the user taps the attachment button.
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
            ChatTopBarComponent(
                title = state.chatTitle ?: stringResource(R.string.new_chat),
                onBackClick = onBackClick,
                onOptionsClick = onOptionsClick,
            )
            if (state.isOffline) {
                OfflineBannerComponent()
            }
            if (state.status is ChatStatus.Failed) {
                ErrorBannerComponent(
                    message = failureText(state.status.reason),
                    onRetryClick = onRetryClick,
                    onDismissClick = onDismissError,
                )
            }
            MessageListComponent(
                messages = state.messages,
                isWaitingForReply = state.status is ChatStatus.Waiting,
                onEditMessage = onEditMessage,
                onCopyMessage = onCopyMessage,
                onRegenerateMessage = onRegenerateMessage,
                onSuggestionClick = onInputChange,
                assembleLogo = assembleLogo,
                logoColors = logoColors,
                modifier = Modifier.weight(1f),
            )
            ChatInputComponent(
                text = state.inputText,
                onTextChange = onInputChange,
                attachedImage = state.attachedImage,
                onRemoveAttachment = onRemoveAttachment,
                isInputEnabled = !state.isOffline,
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

/** The message shown in the error banner for [reason]. */
@Composable
private fun failureText(reason: FailureReason): String =
    when (reason) {
        FailureReason.RATE_LIMITED -> stringResource(R.string.failure_rate_limited)
        FailureReason.SERVER_ERROR -> stringResource(R.string.failure_server_error)
        FailureReason.INVALID_KEY -> stringResource(R.string.failure_invalid_key)
        FailureReason.NO_CONNECTION -> stringResource(R.string.failure_no_connection)
        FailureReason.UNKNOWN -> stringResource(R.string.failure_unknown)
    }

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun FailureTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Column {
            FailureReason.entries.forEach { reason ->
                Text(
                    text = failureText(reason),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

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
            state = previewNormalState.copy(status = ChatStatus.Waiting),
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
            state = previewNormalState.copy(status = ChatStatus.Failed(FailureReason.RATE_LIMITED)),
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
private fun ActiveChatScreenInvalidKeyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(status = ChatStatus.Failed(FailureReason.INVALID_KEY)),
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