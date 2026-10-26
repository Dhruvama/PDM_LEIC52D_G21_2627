package isel.pdm.droidmentor.activechat

import android.content.ClipData
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.buildLogTag
import isel.pdm.droidmentor.activechat.components.ChatInputComponent
import isel.pdm.droidmentor.activechat.components.ChatTopBarComponent
import isel.pdm.droidmentor.activechat.components.ErrorBannerComponent
import isel.pdm.droidmentor.activechat.components.MessageListComponent
import isel.pdm.droidmentor.activechat.components.OfflineBannerComponent
import isel.pdm.droidmentor.domain.chat.Author
import isel.pdm.droidmentor.domain.chat.ChatMessage
import isel.pdm.droidmentor.domain.chat.FailureReason
import isel.pdm.droidmentor.ui.components.animations.particlelogo.ParticleLogoColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import kotlinx.coroutines.launch
import java.net.URI

const val ACTIVE_CHAT_SCREEN_TAG = "active_chat_screen"

private val logTag = buildLogTag("ActiveChatScreen")

/**
 * Root of the Active Chat screen: connects [viewModel] to the stateless screen content.
 *
 * @param viewModel Holds the conversation and handles the user's actions.
 * @param assembleLogo `true` when coming from the Title screen, so the welcome's logo
 * gathers from its cloud.
 * @param onBack Called when the user taps the back button.
 * @param onHistory Called when the user taps the history button.
 */
@Composable
fun ActiveChatScreen(
    viewModel: ActiveChatScreenViewModel,
    assembleLogo: Boolean,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val clipboard = LocalClipboard.current
    // setClipEntry is a suspend function, so copying runs in a coroutine tied to this screen.
    val scope = rememberCoroutineScope()

    // Created once and reused across recompositions; the keys recreate it only if one of them changes.
    val actions = remember(viewModel, onBack, onHistory, onSettings, clipboard, scope) {
        object : ActiveChatActions {
            // `.invoke()` makes it explicit that these call the screen's parameters, not this object's own functions.
            override fun onBack() = onBack.invoke()

            override fun onHistory() = onHistory.invoke()

            override fun onSettings() = onSettings.invoke()

            override fun onInputChange(text: String) = viewModel.updateInput(text)

            override fun onAttach() {
                Log.v(logTag, "Attach clicked")
                // TODO (phase E): pick an image from the gallery
                //viewModel.attachImage(uri = uri)
            }

            override fun onCamera() {
                Log.v(logTag, "Camera clicked")
                // TODO (phase E): take a photo
                //viewModel.attachImage(uri = uri)
            }

            override fun onRemoveAttachment() = viewModel.removeAttachedImage()

            override fun onSend() = viewModel.send()

            override fun onEditMessage(message: ChatMessage) {
                Log.v(logTag, "Edit message ${message.id} clicked")
                //viewModel.startEditing(message_id = message.id)
                // TODO (phase D): edit the message
            }

            override fun onCancelEdit() {
                Log.v(logTag, "Cancel edit clicked")
                //viewModel.cancelEditing()
                // TODO (phase D): leave edit mode
            }

            // Block body, not `= scope.launch { }`: launch returns a Job, and the interface expects Unit.
            override fun onCopyMessage(message: ChatMessage) {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("message", message.text)))
                }
            }

            override fun onRegenerateMessage(message: ChatMessage) {
                Log.v(logTag, "Regenerate message ${message.id} clicked")
                //viewModel.regenerateMessage(message_id = message.id)
                // TODO (phase D): regenerate the reply
            }

            override fun onRetry() {
                Log.v(logTag, "Retry clicked")
                //viewModel.retry()
                // TODO (phase C): retry the request to Gemini
            }

            override fun onDismissError() = viewModel.dismissError()
        }
    }

    // The content handles the insets and draws its own background.
    Scaffold(containerColor = Color.Transparent, contentWindowInsets = WindowInsets(0)) { innerPadding ->
        ActiveChatScreenContent(
            state = viewModel.state,
            actions = actions,
            assembleLogo = assembleLogo,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/**
 * Draws the whole Active Chat screen from [state]: top bar, status banners, message list
 * and composer. Stateless: every user action is passed to [actions].
 *
 * @param state What the screen shows.
 * @param actions What happens when the user interacts with the screen.
 * @param modifier Modifier for the screen's root.
 * @param assembleLogo `true` when coming from the Title screen, so the welcome's logo
 * gathers from its cloud. The Activity sets it from an Intent extra.
 * @param logoColors Colors of the welcome's logo chosen by the user, or `null` to use the theme.
 * Will come from the user's settings.
 */
@Composable
private fun ActiveChatScreenContent(
    state: ActiveChatScreenState,
    actions: ActiveChatActions,
    modifier: Modifier = Modifier,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current)
            .testTag(ACTIVE_CHAT_SCREEN_TAG),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBarComponent(
                title = state.chatTitle ?: stringResource(R.string.new_chat),
                onBackClick = actions::onBack,
                onHistoryClick = actions::onHistory,
                onSettingsClick = actions::onSettings,
            )
            if (state.isOffline) {
                OfflineBannerComponent()
            }
            if (state.status is ChatStatus.Failed) {
                ErrorBannerComponent(
                    message = failureText(state.status.reason),
                    onRetryClick = actions::onRetry,
                    onDismissClick = actions::onDismissError,
                )
            }
            MessageListComponent(
                messages = state.messages,
                isWaitingForReply = state.status is ChatStatus.Waiting,
                onEditMessage = actions::onEditMessage,
                onCopyMessage = actions::onCopyMessage,
                onRegenerateMessage = actions::onRegenerateMessage,
                onSuggestionClick = actions::onInputChange,
                assembleLogo = assembleLogo,
                logoColors = logoColors,
                editingMessageId = state.editingMessageId,
                modifier = Modifier.weight(1f),
            )
            ChatInputComponent(
                text = state.inputText,
                onTextChange = actions::onInputChange,
                attachedImage = state.attachedImage,
                onRemoveAttachment = actions::onRemoveAttachment,
                isInputEnabled = !state.isOffline,
                isSendEnabled = state.canSend,
                onAttachClick = actions::onAttach,
                onCameraClick = actions::onCamera,
                onSendClick = actions::onSend,
                isEditing = state.isEditing,
                onCancelEdit = actions::onCancelEdit,
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

/** Actions that do nothing, for previews. */
private object PreviewActions : ActiveChatActions {
    override fun onBack() {}
    override fun onHistory() {}
    override fun onSettings() {}
    override fun onInputChange(text: String) {}
    override fun onAttach() {}
    override fun onCamera() {}
    override fun onRemoveAttachment() {}
    override fun onSend() {}
    override fun onEditMessage(message: ChatMessage) {}
    override fun onCancelEdit() {}
    override fun onCopyMessage(message: ChatMessage) {}
    override fun onRegenerateMessage(message: ChatMessage) {}
    override fun onRetry() {}
    override fun onDismissError() {}
}

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
        ActiveChatScreenContent(state = previewNormalState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenNewChatPreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(state = ActiveChatScreenState(), actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenNewChatWithCustomLogoColorsPreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(
            state = ActiveChatScreenState(),
            actions = PreviewActions,
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
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenOfflinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(isOffline = true),
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenErrorPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(status = ChatStatus.Failed(FailureReason.RATE_LIMITED)),
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenInvalidKeyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(status = ChatStatus.Failed(FailureReason.INVALID_KEY)),
            actions = PreviewActions,
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
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenLightThemePreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ActiveChatScreenContent(state = previewNormalState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenVioletThemePreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        ActiveChatScreenContent(state = previewNormalState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 860, heightDp = 400)
@Composable
private fun ActiveChatScreenLandscapePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(state = previewNormalState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 860, heightDp = 400)
@Composable
private fun ActiveChatScreenNewChatLandscapePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(state = ActiveChatScreenState(), actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenEditingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(state = previewEditingState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenEditingFirstMessagePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(
                editingMessageId = previewMessages[0].id,
                inputText = previewMessages[0].text,
            ),
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenEditingWithImagePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(
            state = previewNormalState.copy(
                messages = previewMessagesWithImage,
                editingMessageId = previewImageMessage.id,
                inputText = previewImageMessage.text,
                attachedImage = Uri.parse(previewImageMessage.imageUri.toString()),
            ),
            actions = PreviewActions,
        )
    }
}

@Preview(showBackground = true, device = "spec:width=860dp,height=400dp,orientation=landscape")
@Composable
private fun ActiveChatScreenEditingLandscapePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ActiveChatScreenContent(state = previewEditingState, actions = PreviewActions)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ActiveChatScreenInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var state by remember { mutableStateOf(previewNormalState) }
        // Same as PreviewActions, except typing updates the preview's state.
        val actions = remember {
            object : ActiveChatActions by PreviewActions {
                override fun onInputChange(text: String) {
                    state = state.copy(inputText = text)
                }
            }
        }
        ActiveChatScreenContent(state = state, actions = actions)
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

/** Editing the 2nd user message, with its text slightly changed. */
private val previewEditingState = previewNormalState.copy(
    editingMessageId = previewMessages[2].id,
    inputText = "How do I give them stable keys in a LazyColumn?",
)

/** The 2nd user message of [previewMessages], with an image. */
private val previewImageMessage = previewMessages[2].copy(
    text = "How do I give them stable keys? Here is my list:",
    imageUri = URI.create("content://preview/image"),
)

private val previewMessagesWithImage = previewMessages.map {
    if (it.id == previewImageMessage.id) previewImageMessage else it
}