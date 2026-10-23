package isel.pdm.droidmentor.activechat.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val EXPANDED_CODE_DIALOG_TAG = "expanded_code_dialog"
const val CLOSE_EXPANDED_CODE_BUTTON_TAG = "close_expanded_code_button"

/**
 * A centered window that shows a code block in full, scrollable in both directions.
 *
 * The back button, tapping outside the window and the close button all close it.
 * Behind the window the screen is dimmed and, on Android 12+, blurred.
 *
 * @param code The code to show.
 * @param onDismiss Called when the window should close.
 */
@Composable
fun ExpandedCodeDialogComponent(code: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window

        // Below Android 12, or if the device disables cross-window blur, only the dim is shown.
        SideEffect {
            if (window != null) {
                window.setDimAmount(0.35f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window.attributes = window.attributes.also { it.blurBehindRadius = 40 }
                }
            }
        }

        ComponentExpandedCodeContent(
            code = code,
            onCloseClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f),
        )
    }
}

/**
 * What is inside [ExpandedCodeDialogComponent]: a header with a title and a close button, and the code
 * below it. Separate from the dialog so it can be previewed.
 *
 * @param code The code to show.
 * @param onCloseClick Called when the user taps the close button.
 * @param modifier Modifier applied to the window surface.
 */
@Composable
fun ComponentExpandedCodeContent(
    code: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier.testTag(EXPANDED_CODE_DIALOG_TAG),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.borderSubtle),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.expanded_code_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                ComponentBackButton(
                    icon = Icons.Default.Close,
                    onCloseClick = onCloseClick,
                    modifier = Modifier.testTag(CLOSE_EXPANDED_CODE_BUTTON_TAG),
                )
            }
            HorizontalDivider(color = colors.borderSubtle)
            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.textPrimary,
                softWrap = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colors.background)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}

/**
 * Icon button that closes the window it is in.
 *
 * @param icon The icon to show, e.g. [Icons.Default.Close].
 * @param onCloseClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
fun ComponentBackButton(
    icon: ImageVector,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onCloseClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.expanded_code_close),
            tint = LocalAppColors.current.textPrimary,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun ComponentExpandedCodeContentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentExpandedCodeContent(code = previewLongCode, onCloseClick = { })
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun ComponentExpandedCodeContentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentExpandedCodeContent(code = previewLongCode, onCloseClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentBackButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentBackButton(icon = Icons.Default.Close, onCloseClick = { })
        }
    }
}

private val previewLongCode = """
    package isel.pdm.droidmentor.activechat

    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.setValue
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.launch

    data class ChatState(
        val messages: List<String> = emptyList(),
        val input: String = "",
        val isLoading: Boolean = false,
    )

    class ChatViewModel(private val service: MentorService) : ViewModel() {

        var state by mutableStateOf(ChatState())
            private set

        fun onInputChange(text: String) {
            state = state.copy(input = text)
        }

        fun send() {
            if (state.isLoading || state.input.isBlank()) return
            val question = state.input
            state = state.copy(messages = state.messages + question, input = "", isLoading = true)
            viewModelScope.launch {
                val answer = service.ask(question = question, history = state.messages, temperature = 0.7f, maxOutputTokens = 2048, systemInstruction = "You are a senior Android mentor.")
                state = state.copy(messages = state.messages + answer, isLoading = false)
            }
        }

        fun clear() {
            if (state.isLoading) return
            state = ChatState()
        }
    }

    interface MentorService {
        suspend fun ask(question: String, history: List<String>, temperature: Float, maxOutputTokens: Int, systemInstruction: String): String
    }
""".trimIndent()