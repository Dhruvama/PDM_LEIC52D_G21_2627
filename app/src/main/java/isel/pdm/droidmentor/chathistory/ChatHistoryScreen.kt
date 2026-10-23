package isel.pdm.droidmentor.chathistory

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.chathistory.components.ChatHistoryItem
import isel.pdm.droidmentor.chathistory.components.ChatHistorySnackbarHost
import isel.pdm.droidmentor.chathistory.components.ChatHistoryTopBar
import isel.pdm.droidmentor.chathistory.components.ChatSearchField
import isel.pdm.droidmentor.chathistory.components.EmptyHistory
import isel.pdm.droidmentor.chathistory.components.NewChatButton
import isel.pdm.droidmentor.chathistory.components.NoResults
import isel.pdm.droidmentor.chathistory.components.UndoDeleteSnackbarEffect
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val CHAT_HISTORY_SCREEN_TAG = "chat_history_screen"
const val CHAT_HISTORY_LIST_TAG = "chat_history_list"
const val CHAT_HISTORY_LOADING_TAG = "chat_history_loading"

/** Keeps the list readable on tablets and in landscape. */
private val CONTENT_MAX_WIDTH = 640.dp

/**
 * Root of the Chat History screen: connects [viewModel] to the stateless screen content.
 *
 * @param viewModel Holds the saved chats and handles the user's actions.
 * @param onBack Called when the user taps the back button.
 * @param onOpenChat Called with the chat the user tapped, to open it.
 * @param onNewChat Called when the user taps "New chat".
 */
@Composable
fun ChatHistoryScreen(
    viewModel: ChatHistoryScreenViewModel,
    onBack: () -> Unit,
    onOpenChat: (ChatSummary) -> Unit,
    onNewChat: () -> Unit,
) {
    ChatHistoryScreenContent(
        state = viewModel.state,
        deletedChat = viewModel.deletedChat,
        onBackClick = onBack,
        onSearchChange = { viewModel.updateSearch(it) },
        onChatClick = onOpenChat,
        onDeleteChat = { viewModel.deleteChat(it) },
        onUndoDelete = { viewModel.undoDelete() },
        onDeleteConfirmed = { viewModel.confirmDelete() },
        onNewChatClick = onNewChat,
    )
}

/**
 * Draws the whole Chat History screen from [state]: top bar, search field and the list of
 * saved chats (or a loading indicator, the empty state or a "no results" message), plus the
 * "Chat deleted · Undo" snackbar while [deletedChat] isn't `null`.
 * Stateless: every user action is passed straight to a callback.
 *
 * @param state What the screen shows.
 * @param deletedChat The chat just deleted, while it can still be undone; `null` otherwise.
 * @param onBackClick Called when the user taps the back button.
 * @param onSearchChange Called with the new text whenever the user types in the search field.
 * @param onChatClick Called with the chat the user tapped, to open it.
 * @param onDeleteChat Called with the chat whose "Delete" button was tapped.
 * @param onUndoDelete Called when the user taps "Undo" in the snackbar.
 * @param onDeleteConfirmed Called when the snackbar goes away without "Undo" being tapped.
 * @param onNewChatClick Called when the user taps "New chat".
 */
@Composable
private fun ChatHistoryScreenContent(
    state: ChatHistoryScreenState,
    deletedChat: ChatSummary?,
    onBackClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onChatClick: (ChatSummary) -> Unit,
    onDeleteChat: (ChatSummary) -> Unit,
    onUndoDelete: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onNewChatClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UndoDeleteSnackbarEffect(
        deletedChat = deletedChat,
        snackbarHostState = snackbarHostState,
        onUndo = onUndoDelete,
        onConfirmed = onDeleteConfirmed,
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .glowBackground(LocalAppColors.current)
            .testTag(CHAT_HISTORY_SCREEN_TAG),
        // Without this the Scaffold paints over the glow
        containerColor = Color.Transparent,
        // safeDrawing includes the keyboard: the FAB and the list move up when it opens
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChatHistoryTopBar(onBackClick = onBackClick)
                if (state is ChatHistoryScreenState.Loaded) {
                    ChatSearchField(
                        query = state.searchQuery,
                        onQueryChange = onSearchChange,
                        matchCount = state.visibleChats.size,
                        modifier = Modifier
                            .widthIn(max = CONTENT_MAX_WIDTH)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        },
        floatingActionButton = {
            // The empty state already has its own button with animation
            if (state is ChatHistoryScreenState.Loaded) {
                NewChatButton(onClick = onNewChatClick)
            }
        },
        snackbarHost = { ChatHistorySnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // Consuming stops EmptyHistory's navigationBarsPadding() from adding the inset twice
        val body = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        when (state) {
            ChatHistoryScreenState.Loading -> Box(body, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = LocalAppColors.current.accent,
                    modifier = Modifier.testTag(CHAT_HISTORY_LOADING_TAG),
                )
            }

            ChatHistoryScreenState.Empty -> EmptyHistory(onNewChatClick = onNewChatClick, modifier = body)

            is ChatHistoryScreenState.Loaded ->
                if (state.visibleChats.isEmpty()) {
                    NoResults(query = state.searchQuery.trim(), modifier = body)
                } else {
                    // The list goes under the navigation bar (edge-to-edge), so the
                    // padding goes inside it instead of around it
                    ChatList(
                        chats = state.visibleChats,
                        onChatClick = onChatClick,
                        onDeleteChat = onDeleteChat,
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
        }
    }
}

/** The saved chats, with room at the bottom so the last one isn't hidden by "New chat". */
@Composable
private fun ChatList(
    chats: List<ChatSummary>,
    onChatClick: (ChatSummary) -> Unit,
    onDeleteChat: (ChatSummary) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag(CHAT_HISTORY_LIST_TAG),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            // The Scaffold doesn't leave room for the FAB
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
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

//==================================== TESTS/PREVIEWS ====================================//

@Composable
private fun PreviewContent(state: ChatHistoryScreenState, colors: AppColors = DefaultDarkColors) {
    DroidMentorTheme(colors = colors) {
        ChatHistoryScreenContent(
            state = state,
            deletedChat = null,
            onBackClick = { },
            onSearchChange = { },
            onChatClick = { },
            onDeleteChat = { },
            onUndoDelete = { },
            onDeleteConfirmed = { },
            onNewChatClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenNormalPreview() = PreviewContent(previewNormalState)

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenEmptyPreview() = PreviewContent(ChatHistoryScreenState.Empty, PresetThemes[1])

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenLoadingPreview() =
    PreviewContent(ChatHistoryScreenState.Loading)

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

// Search, delete and undo work: check it in Interactive Mode.
@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ChatHistoryScreenInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var chats by remember { mutableStateOf(previewChats) }
        var searchQuery by remember { mutableStateOf("") }
        var deletedChat by remember { mutableStateOf<ChatSummary?>(null) }
        val shown = chats.filter { it != deletedChat }
        ChatHistoryScreenContent(
            state =
                if (shown.isEmpty()) ChatHistoryScreenState.Empty
                else ChatHistoryScreenState.Loaded(shown, searchQuery),
            deletedChat = deletedChat,
            onBackClick = { },
            onSearchChange = { searchQuery = it },
            onChatClick = { },
            onDeleteChat = { chat ->
                deletedChat?.let { chats = chats - it }
                deletedChat = chat
            },
            onUndoDelete = { deletedChat = null },
            onDeleteConfirmed = {
                deletedChat?.let { chats = chats - it }
                deletedChat = null
            },
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

private val previewNormalState = ChatHistoryScreenState.Loaded(chats = previewChats)