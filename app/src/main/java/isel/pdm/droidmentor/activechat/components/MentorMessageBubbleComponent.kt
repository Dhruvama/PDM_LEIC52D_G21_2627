package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.chat.Author
import isel.pdm.droidmentor.domain.chat.ChatMessage
import isel.pdm.droidmentor.domain.chat.MessagePart
import isel.pdm.droidmentor.domain.chat.splitIntoParts
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val MENTOR_COPY_BUTTON_TAG = "mentor_copy_button"
const val MENTOR_REGENERATE_BUTTON_TAG = "mentor_regenerate_button"

/**
 * A message written by the mentor, aligned to the start of the screen, with an avatar.
 *
 * Text between lines is shown as a [CodeBlockComponent]; the rest is plain text.
 * Under the content there are "Copy" and "Regenerate" actions.
 *
 * @param message The message to show. Expected to have [Author.MENTOR].
 * @param onCopyClick Called when the user taps "Copy".
 * @param onRegenerateClick Called when the user taps "Regenerate".
 * @param modifier Modifier applied to the whole row.
 * @param isAvatarAnimated `true` while this answer is still being written (streaming, later).
 */
@Composable
fun MentorMessageBubbleComponent(
    message: ChatMessage,
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAvatarAnimated: Boolean = false,
) {
    val colors = LocalAppColors.current
    val parts = remember(message.text) { splitIntoParts(message.text) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        MentorAvatarComponent(animated = isAvatarAnimated)
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .frostedGlass(
                    colors,
                    RoundedCornerShape(
                        topStart = 6.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 20.dp,
                    ),
                )
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (part in parts) {
                when (part) {
                    is MessagePart.Plain -> Text(
                        text = part.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                    )
                    is MessagePart.Code -> CodeBlockComponent(code = part.code)
                }
            }
            ComponentMentorActions(
                onCopyClick = onCopyClick,
                onRegenerateClick = onRegenerateClick,
            )
        }
    }
}

/**
 * The "Copy" and "Regenerate" actions under the mentor's message.
 *
 * @param onCopyClick Called when the user taps "Copy".
 * @param onRegenerateClick Called when the user taps "Regenerate".
 */
@Composable
private fun ComponentMentorActions(
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ComponentCopyButton(onClick = onCopyClick)
        ComponentRegenerateButton(onClick = onRegenerateClick)
    }
}

/**
 * Text button that copies the mentor's message.
 * A text button because material-icons-core has no copy icon.
 *
 * @param onClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
fun ComponentCopyButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.testTag(MENTOR_COPY_BUTTON_TAG),
    ) {
        Text(
            text = stringResource(R.string.mentor_message_copy),
            style = MaterialTheme.typography.labelMedium,
            color = LocalAppColors.current.textSecondary,
        )
    }
}

/**
 * Small 32.dp icon button that asks the mentor to write the answer again.
 *
 * @param onClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
fun ComponentRegenerateButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(32.dp)
            .testTag(MENTOR_REGENERATE_BUTTON_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.mentor_message_regenerate),
            tint = LocalAppColors.current.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(widthDp = 400)
@Composable
private fun MentorMessageBubbleComponentTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            MentorMessageBubbleComponent(
                message = previewTextMessage,
                onCopyClick = { },
                onRegenerateClick = { },
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorMessageBubbleComponentTextLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            MentorMessageBubbleComponent(
                message = previewTextMessage,
                onCopyClick = { },
                onRegenerateClick = { },
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorMessageBubbleComponentCodePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            MentorMessageBubbleComponent(
                message = previewCodeMessage,
                onCopyClick = { },
                onRegenerateClick = { },
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorMessageBubbleComponentCodeLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            MentorMessageBubbleComponent(
                message = previewCodeMessage,
                onCopyClick = { },
                onRegenerateClick = { },
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorMessageBubbleComponentTextAfterCodePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            MentorMessageBubbleComponent(
                message = previewTextAfterCodeMessage,
                onCopyClick = { },
                onRegenerateClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun ComponentMentorActionsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentMentorActions(onCopyClick = { }, onRegenerateClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentCopyButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentCopyButton(onClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentRegenerateButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentRegenerateButton(onClick = { })
        }
    }
}

private val previewTextMessage = ChatMessage(
    id = 1L,
    author = Author.MENTOR,
    text = "Use a ViewModel: it survives configuration changes, while remember does not.",
)

private val previewCodeMessage = ChatMessage(
    id = 2L,
    author = Author.MENTOR,
    text = """
        Here is the idea:
        ```kotlin
        class CounterViewModel : ViewModel() {
            var count by mutableStateOf(0)
                private set
        }
        ```
    """.trimIndent(),
)

private val previewTextAfterCodeMessage = ChatMessage(
    id = 3L,
    author = Author.MENTOR,
    text = """
        Get it in the Activity like this:
        ```kotlin
        private val viewModel by viewModels<CounterViewModel>()
        ```
        The same instance comes back after a rotation.
    """.trimIndent(),
)