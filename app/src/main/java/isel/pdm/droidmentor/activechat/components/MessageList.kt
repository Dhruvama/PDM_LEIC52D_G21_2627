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
import isel.pdm.droidmentor.activechat.Author
import isel.pdm.droidmentor.activechat.ChatMessage
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glassCard
import java.util.Calendar

const val MESSAGE_LIST_TAG = "message_list"
const val EMPTY_CHAT_WELCOME_TAG = "empty_chat_welcome"
const val SUGGESTION_CHIP_TAG = "suggestion_chip"

/**
 * The scrollable list of messages of the Active Chat screen.
 *
 * Shows a welcome with suggestions instead of the list when there are no messages
 * and no reply is pending. While a reply is pending, a [ThinkingIndicator] is shown
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
 * (entry transition). See [ParticleLogo].
 * @param logoColors Colors of the welcome's logo chosen by the user, or `null` to use the theme.
 * @param modifier Modifier applied to the list (or to the welcome's container when empty).
 */
@Composable
fun MessageList(
    messages: List<ChatMessage>,
    isWaitingForReply: Boolean,
    onEditMessage: (ChatMessage) -> Unit,
    onCopyMessage: (ChatMessage) -> Unit,
    onRegenerateMessage: (ChatMessage) -> Unit,
    onSuggestionClick: (String) -> Unit,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
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

    if (messages.isEmpty() && !isWaitingForReply) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            EmptyChatWelcome(
                onSuggestionClick = onSuggestionClick,
                assembleLogo = assembleLogo,
                logoColors = logoColors,
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.testTag(MESSAGE_LIST_TAG),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(messages, key = { it.id }, contentType = { it.author }) { message ->
                when (message.author) {
                    Author.USER -> UserMessageBubble(
                        message = message,
                        onEditClick = { onEditMessage(message) },
                    )
                    Author.MENTOR -> MentorMessageBubble(
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
                    ThinkingIndicator()
                }
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
private fun EmptyChatWelcome(
    onSuggestionClick: (String) -> Unit,
    assembleLogo: Boolean = false,
    logoColors: ParticleLogoColors? = null,
) {
    val c = LocalAppColors.current
    // In landscape a bigger logo pushes the suggestions off screen.
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
        ParticleLogo(
            size = logoSize,
            glow = !c.isLight,
            logoColors = logoColors,
            assembleOnStart = assembleLogo,
        )
        WelcomeGreeting()
        SuggestionChips(onSuggestionClick = onSuggestionClick)
    }
}

/**
 * "Good morning/afternoon/evening!" depending on the hour, followed by the welcome question.
 */
@Composable
private fun WelcomeGreeting() {
    val c = LocalAppColors.current
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
        color = c.textPrimary,
        textAlign = TextAlign.Center,
    )
}

/**
 * The three suggestions, centered and wrapping to a new line when they do not fit.
 */
@Composable
private fun SuggestionChips(onSuggestionClick: (String) -> Unit) {
    val reviewViewModel = stringResource(R.string.suggestion_review_viewmodel)
    val explainRecomposition = stringResource(R.string.suggestion_explain_recomposition)
    val roomOrDataStore = stringResource(R.string.suggestion_room_or_datastore)

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WelcomeSuggestionChip(
            text = reviewViewModel,
            onClick = { onSuggestionClick(reviewViewModel) },
        )
        WelcomeSuggestionChip(
            text = explainRecomposition,
            onClick = { onSuggestionClick(explainRecomposition) },
        )
        WelcomeSuggestionChip(
            text = roomOrDataStore,
            onClick = { onSuggestionClick(roomOrDataStore) },
        )
    }
}

/**
 * One rounded glass suggestion chip. Tapping it reports its [text] through [onClick].
 */
@Composable
private fun WelcomeSuggestionChip(text: String, onClick: () -> Unit) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .height(40.dp)
            .glassCard(c, CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = 14.dp)
            .testTag(SUGGESTION_CHIP_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = c.textPrimary,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun MessageListConversationPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MessageList(
            messages = previewConversation,
            isWaitingForReply = false,
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onSuggestionClick = { },
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun MessageListWaitingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MessageList(
            messages = previewConversation,
            isWaitingForReply = true,
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onSuggestionClick = { },
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun MessageListEmptyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MessageList(
            messages = emptyList(),
            isWaitingForReply = false,
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onSuggestionClick = { },
        )
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun MessageListEmptyLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        MessageList(
            messages = emptyList(),
            isWaitingForReply = false,
            onEditMessage = { },
            onCopyMessage = { },
            onRegenerateMessage = { },
            onSuggestionClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyChatWelcomePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        EmptyChatWelcome(onSuggestionClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeGreetingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        WelcomeGreeting()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SuggestionChipsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        SuggestionChips(onSuggestionClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeSuggestionChipPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        WelcomeSuggestionChip(text = "Explain recomposition", onClick = { })
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