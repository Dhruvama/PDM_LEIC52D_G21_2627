package isel.pdm.droidmentor.activechat.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.chat.Author
import isel.pdm.droidmentor.domain.chat.ChatMessage
import isel.pdm.droidmentor.ui.components.animations.particlelogo.ParticleLogoColors
import isel.pdm.droidmentor.ui.components.animations.particlelogo.ParticleLogoComponent
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import java.util.Calendar

const val MESSAGE_LIST_TAG = "message_list"
const val EMPTY_CHAT_WELCOME_TAG = "empty_chat_welcome"
const val SUGGESTION_CHIP_TAG = "suggestion_chip"

/**
 * The scrollable list of messages of the Active Chat screen.
 *
 * Shows a welcome with suggestions instead of the list when there are no messages
 * and no reply is pending. While a reply is pending, a [ThinkingIndicatorComponent] is shown
 * after the last message. Scrolls to the bottom whenever a message is added or the
 * thinking indicator appears or disappears.
 *
 * @param messages Messages to show, oldest first.
 * @param isWaitingForReply `true` while the mentor's reply is pending.
 * @param onEditMessage Called with the user's message whose "Edit" button was tapped.
 * @param onCopyMessage Called with the mentor's message whose "Copy" button was tapped.
 * @param onRegenerateMessage Called with the mentor's message whose "Regenerate" button was tapped.
 * @param onSuggestionClick Called with the text of the tapped suggestion, to fill the input with it.
 * @param assembleLogo `true` to make the welcome's logo gather from the Title screen's cloud
 * (entry transition). See [ParticleLogoComponent].
 * @param logoColors Colors of the welcome's logo chosen by the user, or `null` to use the theme.
 * @param modifier Modifier applied to the list (or to the welcome's container when empty).
 */
@Composable
fun MessageListComponent(
    messages: List<ChatMessage>,
    isWaitingForReply: Boolean,
    onEditMessage: (ChatMessage) -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
) {
    if (messages.isEmpty() && !isWaitingForReply) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ComponentEmptyChatWelcome(
                onSuggestionClick = onSuggestionClick,
                assembleLogo = assembleLogo,
                logoColors = logoColors,
            )
        }
    } else {
        ComponentMessages(
            messages = messages,
            isWaitingForReply = isWaitingForReply,
            onEditMessage = onEditMessage,
            onCopyMessage = onCopyMessage,
            onRegenerateMessage = onRegenerateMessage,
            modifier = modifier,
        )
    }
}

/**
 * The messages themselves, followed by a [ThinkingIndicatorComponent] while a reply is pending.
 * Scrolls to the bottom whenever a message is added or the thinking indicator
 * appears or disappears.
 *
 * @param messages Messages to show, oldest first.
 * @param isWaitingForReply `true` while the mentor's reply is pending.
 * @param onEditMessage Called with the user's message whose "Edit" button was tapped.
 * @param onCopyMessage Called with the mentor's message whose "Copy" button was tapped.
 * @param onRegenerateMessage Called with the mentor's message whose "Regenerate" button was tapped.
 * @param modifier Modifier applied to the list.
 */
@Composable
private fun ComponentMessages(
    messages: List<ChatMessage>,
    isWaitingForReply: Boolean,
    onEditMessage: (ChatMessage) -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isWaitingForReply) {
        var lastIndex = messages.size - 1
        if (isWaitingForReply) lastIndex += 1
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.testTag(MESSAGE_LIST_TAG),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }, contentType = { it.author }) { message ->
            when (message.author) {
                Author.USER -> UserMessageBubbleComponent(
                    message = message,
                    onEditClick = { onEditMessage(message) },
                )
                Author.MENTOR -> MentorMessageBubbleComponent(
                    message = message,
                    onCopyClick = { onCopyMessage(message) },
                    onRegenerateClick = { onRegenerateMessage(message) },
                    // TODO: true for the message being streamed, once Gemini streaming exists
                    isAvatarAnimated = false,
                )
            }
        }
        if (isWaitingForReply) {
            item(key = "thinking") {
                ThinkingIndicatorComponent()
            }
        }
    }
}

/**
 * Shown instead of the list when the chat has no messages yet:
 * the mentor's logo, a greeting for the time of day, and three suggestions.
 * Nothing here clips, so the logo's entry cloud can draw outside the logo's bounds.
 */
@Composable
private fun ComponentEmptyChatWelcome(
    onSuggestionClick: (String) -> Unit,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
) {
    val colors = LocalAppColors.current
    // In landscape a bigger logo pushes the suggestions off-screen.
    val logoSize = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        150.dp
    } else {
        240.dp
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .testTag(EMPTY_CHAT_WELCOME_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        ParticleLogoComponent(
            size = logoSize,
            glow = !colors.isLight,
            logoColors = logoColors,
            assembleOnStart = assembleLogo,
        )
        ComponentWelcomeGreeting()
        ComponentSuggestionChips(onSuggestionClick = onSuggestionClick)
    }
}

/**
 * "Good morning/afternoon/evening!" depending on the hour, followed by the welcome question.
 */
@Composable
private fun ComponentWelcomeGreeting() {
    val colors = LocalAppColors.current
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeGreeting = if (hour in 5..11) {
        stringResource(R.string.welcome_good_morning)
    } else if (hour in 12..18) {
        stringResource(R.string.welcome_good_afternoon)
    } else {
        stringResource(R.string.welcome_good_evening)
    }
    val question = stringResource(R.string.welcome_question)

    Text(
        text = timeGreeting + "\n" + question,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.textPrimary,
        textAlign = TextAlign.Center,
    )
}

/**
 * The three suggestions, centered and wrapping to a new line when they do not fit.
 */
@Composable
private fun ComponentSuggestionChips(onSuggestionClick: (String) -> Unit) {
    val reviewViewModel = stringResource(R.string.suggestion_review_viewmodel)
    val explainRecomposition = stringResource(R.string.suggestion_explain_recomposition)
    val roomOrDataStore = stringResource(R.string.suggestion_room_or_datastore)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ComponentWelcomeSuggestionChip(
            text = reviewViewModel,
            onClick = { onSuggestionClick(reviewViewModel) },
        )
        ComponentWelcomeSuggestionChip(
            text = explainRecomposition,
            onClick = { onSuggestionClick(explainRecomposition) },
        )
        ComponentWelcomeSuggestionChip(
            text = roomOrDataStore,
            onClick = { onSuggestionClick(roomOrDataStore) },
        )
    }
}

/**
 * One rounded glass suggestion chip. Tapping it reports its [text] through [onClick].
 */
@Composable
private fun ComponentWelcomeSuggestionChip(text: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .height(40.dp)
            .frostedGlass(colors, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = 14.dp)
            .testTag(SUGGESTION_CHIP_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun MessageListComponentConversationPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            MessageListComponent(
                messages = previewConversation,
                isWaitingForReply = false,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
                onSuggestionClick = { },
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun MessageListComponentConversationLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            MessageListComponent(
                messages = previewConversation,
                isWaitingForReply = false,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
                onSuggestionClick = { },
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun MessageListComponentWaitingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            MessageListComponent(
                messages = previewConversation,
                isWaitingForReply = true,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
                onSuggestionClick = { },
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun MessageListComponentEmptyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            MessageListComponent(
                messages = emptyList(),
                isWaitingForReply = false,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
                onSuggestionClick = { },
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun MessageListComponentEmptyLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            MessageListComponent(
                messages = emptyList(),
                isWaitingForReply = false,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
                onSuggestionClick = { },
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 640)
@Composable
private fun ComponentMessagesPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.fillMaxSize().glowBackground(LocalAppColors.current)) {
            ComponentMessages(
                messages = previewConversation,
                isWaitingForReply = true,
                onEditMessage = { },
                onCopyMessage = { },
                onRegenerateMessage = { },
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun ComponentEmptyChatWelcomePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(vertical = 16.dp)) {
            ComponentEmptyChatWelcome(onSuggestionClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentWelcomeGreetingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentWelcomeGreeting()
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun ComponentSuggestionChipsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentSuggestionChips(onSuggestionClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentWelcomeSuggestionChipPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentWelcomeSuggestionChip(text = "Explain recomposition", onClick = { })
        }
    }
}

private val previewConversation = listOf(
    ChatMessage(
        id = 1L,
        author = Author.USER,
        text = "My counter resets to 0 when I rotate the phone. Why?",
    ),
    ChatMessage(
        id = 2L,
        author = Author.MENTOR,
        text = "remember only survives recomposition. A rotation recreates the Activity, " +
            "so the state is lost. Keep it in a ViewModel instead.",
    ),
    ChatMessage(
        id = 3L,
        author = Author.USER,
        text = "Can you show me what that looks like?",
    ),
    ChatMessage(
        id = 4L,
        author = Author.MENTOR,
        text = """
            Sure:
            ```kotlin
            class CounterViewModel : ViewModel() {
                var count by mutableStateOf(0)
                    private set

                fun increment() { count++ }
            }
            ```
            Then get it with `by viewModels<CounterViewModel>()` in the Activity.
        """.trimIndent(),
    ),
)