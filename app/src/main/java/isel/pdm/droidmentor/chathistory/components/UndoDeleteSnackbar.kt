package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.history.ChatSummary
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * Shows "Chat deleted · Undo" in [snackbarHostState] each time [deletedChat] changes, and
 * reports whether the user undid it or let the snackbar go away.
 *
 * @param deletedChat The chat just deleted, while it can still be undone; `null` otherwise.
 * @param snackbarHostState The host the snackbar is shown in
 * @param onUndo Called when the user taps "Undo".
 * @param onConfirmed Called when the snackbar goes away without "Undo" being tapped.
 */
@Composable
fun UndoDeleteSnackbarEffect(
    deletedChat: ChatSummary?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onConfirmed: () -> Unit,
) {
    val message = stringResource(R.string.chat_history_chat_deleted)
    val undoLabel = stringResource(R.string.chat_history_undo)

    // Changing the key cancels the previous snackbar; the ViewModel already made that deletion final
    LaunchedEffect(deletedChat) {
        if (deletedChat == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onConfirmed()
        }
    }
}

/** Shows the snackbars of [hostState] in the app's colours instead of Material's defaults. */
@Composable
fun ChatHistorySnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data -> ChatHistorySnackbar(data) }
}

/**
 * One snackbar in the app's colours, with the action in the accent colour. */
@Composable
private fun ChatHistorySnackbar(data: SnackbarData) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)

    Snackbar(
        snackbarData = data,
        shape = shape,
        containerColor = c.surface,
        contentColor = c.textPrimary,
        actionColor = c.accent,
        //TODO() ver se vale a pena ter a snackbar com border, construindo manualmente para evitar os 12dp
    )
}

//==================================== TESTS/PREVIEWS ====================================//

/** Fixed snackbar data, since a real one only exists while [SnackbarHostState] is showing it. */
private class PreviewSnackbarData(message: String, actionLabel: String) : SnackbarData {
    override val visuals = object : SnackbarVisuals {
        override val message = message
        override val actionLabel = actionLabel
        override val withDismissAction = false
        override val duration = SnackbarDuration.Short
    }

    override fun performAction() {}
    override fun dismiss() {}
}

@Composable
private fun PreviewContent(colors: AppColors) {
    DroidMentorTheme(colors = colors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ChatHistorySnackbar(
                PreviewSnackbarData(
                    message = stringResource(R.string.chat_history_chat_deleted),
                    actionLabel = stringResource(R.string.chat_history_undo),
                )
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun UndoDeleteSnackbarDarkPreview() = PreviewContent(DefaultDarkColors)

@Preview(widthDp = 400)
@Composable
private fun UndoDeleteSnackbarLightPreview() = PreviewContent(DefaultLightColors)