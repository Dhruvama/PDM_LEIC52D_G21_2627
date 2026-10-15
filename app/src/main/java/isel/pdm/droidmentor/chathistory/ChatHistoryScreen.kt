package isel.pdm.droidmentor.chathistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.chathistory.components.ChatHistoryItem
import isel.pdm.droidmentor.chathistory.components.ChatHistoryTopBar
import isel.pdm.droidmentor.chathistory.components.ChatSearchField
import isel.pdm.droidmentor.chathistory.components.EmptyHistory
import isel.pdm.droidmentor.chathistory.components.NewChatButton
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val CHAT_HISTORY_SCREEN_TAG = "chat_history_screen"
const val CHAT_HISTORY_LIST_TAG = "chat_history_list"

/** Keeps the list readable on tablets and in landscape. */
private val CONTENT_MAX_WIDTH = 640.dp

// The real ChatHistoryScreen(viewModel, ...) root is added in the ViewModel step.
// This empty stub only exists because ChatHistoryActivity already calls ChatHistoryScreen().
/**
 * Root of the Chat History screen. Empty for now; it will get the ViewModel in a later step.
 */
@Composable
fun ChatHistoryScreen() {
}

/**
 * Draws the whole Chat History screen from [state]: top bar, search field and the list of
 * saved chats (or a loading indicator, the empty state or a "no results" message).
 * Stateless: every user action is passed straight to a callback.
 *
 * @param state What the screen shows.
 * @param onBackClick Called when the user taps the back button.
 * @param onSearchChange Called with the new text whenever the user types in the search field.
 * @param onChatClick Called with the chat the user tapped, to open it.
 * @param onDeleteChat Called with the chat whose "Delete" button was tapped.
 * @param onNewChatClick Called when the user taps "New chat".
 */
@Composable
private fun ChatHistoryScreenContent(
    state: ChatHistoryScreenState,
    onBackClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onChatClick: (ChatSummary) -> Unit,
    onDeleteChat: (ChatSummary) -> Unit,
    onNewChatClick: () -> Unit,
) {
    val hasChats = state.chats.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current)
            .testTag(CHAT_HISTORY_SCREEN_TAG),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatHistoryTopBar(onBackClick = onBackClick)

            if (hasChats) {
                ChatSearchField(
                    query = state.searchQuery,
                    onQueryChange = onSearchChange,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            val body = Modifier
                .weight(1f)
                .fillMaxWidth()
            when {
                state.isLoading -> Box(body, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalAppColors.current.accent)
                }

                !hasChats -> EmptyHistory(onNewChatClick = onNewChatClick, modifier = body)

                state.visibleChats.isEmpty() -> NoResults(query = state.searchQuery.trim(), modifier = body)

                else -> ChatList(
                    chats = state.visibleChats,
                    onChatClick = onChatClick,
                    onDeleteChat = onDeleteChat,
                    modifier = body,
                )
            }
        }

        // The empty state already has its own button
        if (hasChats) {
            NewChatButton(
                onClick = onNewChatClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(20.dp),
            )
        }
    }
}

/** The saved chats, with room at the bottom so the last one isn't hidden by "New chat". */
@Composable
private fun ChatList(
    chats: List<ChatSummary>,
    onChatClick: (ChatSummary) -> Unit,
    onDeleteChat: (ChatSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = modifier.testTag(CHAT_HISTORY_LIST_TAG),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = navigationBarHeight + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(chats, key = { it.id }) { chat ->
            ChatHistoryItem(
                chat = chat,
                onClick = { onChatClick(chat) },
                onDeleteClick = { onDeleteChat(chat) },
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .animateItem(),
            )
        }
    }
}

/** Shown when there are saved chats but none matches the search. */
@Composable
private fun NoResults(query: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            stringResource(R.string.chat_history_no_results, query),
            color = LocalAppColors.current.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Composable
private fun PreviewContent(state: ChatHistoryScreenState, colors: AppColors = DefaultDarkColors) {
    DroidMentorTheme(colors = colors) {
        ChatHistoryScreenContent(
            state = state,
            onBackClick = { },
            onSearchChange = { },
            onChatClick = { },
            onDeleteChat = { },
            onNewChatClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenNormalPreview() = PreviewContent(previewNormalState)

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenEmptyPreview() = PreviewContent(ChatHistoryScreenState(), PresetThemes[1])

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenLoadingPreview() =
    PreviewContent(ChatHistoryScreenState(isLoading = true))

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenSearchPreview() =
    PreviewContent(previewNormalState.copy(searchQuery = "compose"))

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenNoResultsPreview() =
    PreviewContent(previewNormalState.copy(searchQuery = "elden ring"))

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenLightThemePreview() = PreviewContent(previewNormalState, DefaultLightColors)

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenVioletThemePreview() = PreviewContent(previewNormalState, PresetThemes[1])

@Preview(showBackground = true, widthDp = 860, heightDp = 400)
@Composable
private fun ChatHistoryScreenLandscapePreview() = PreviewContent(previewNormalState)

// Search and delete work: check it in Interactive Mode.
@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var state by remember { mutableStateOf(previewNormalState) }
        ChatHistoryScreenContent(
            state = state,
            onBackClick = { },
            onSearchChange = { state = state.copy(searchQuery = it) },
            onChatClick = { },
            onDeleteChat = { chat -> state = state.copy(chats = state.chats - chat) },
            onNewChatClick = { },
        )
    }
}

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

private val previewNow = System.currentTimeMillis()

private val previewChats = listOf(
    ChatSummary(
        id = 1L,
        title = "LazyColumn recomposition",
        lastMessage = "Now only the new item is composed.",
        lastUpdatedMillis = previewNow - 5 * MINUTE,
        messageCount = 4,
    ),
    ChatSummary(
        id = 2L,
        title = "Estado perdido ao rodar o ecrã",
        lastMessage = "Usa rememberSaveable em vez de remember.",
        lastUpdatedMillis = previewNow - 3 * HOUR,
        messageCount = 6,
    ),
    ChatSummary(
        id = 3L,
        title = null,
        lastMessage = "Qual a diferença entre StateFlow e SharedFlow?",
        lastUpdatedMillis = previewNow - DAY,
        messageCount = 1,
    ),
    ChatSummary(
        id = 4L,
        title = "Temas dinâmicos em Compose",
        lastMessage = "dynamicDarkColorScheme só existe a partir do Android 12.",
        lastUpdatedMillis = previewNow - 4 * DAY,
        messageCount = 12,
    ),
    ChatSummary(
        id = 5L,
        title = "Coroutines e o ciclo de vida",
        lastMessage = "viewModelScope cancela tudo quando o ViewModel é limpo.",
        lastUpdatedMillis = previewNow - 20 * DAY,
        messageCount = 9,
    ),
)

private val previewNormalState = ChatHistoryScreenState(chats = previewChats)