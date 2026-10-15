package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.layout.Arrangement
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
import isel.pdm.droidmentor.activechat.Author
import isel.pdm.droidmentor.activechat.ChatMessage
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass

const val MENTOR_COPY_BUTTON_TAG = "mentor_copy_button"
const val MENTOR_REGENERATE_BUTTON_TAG = "mentor_regenerate_button"

/**
 * A message written by the mentor, aligned to the start of the screen, with an avatar.
 *
 * Text between ``` lines is shown as a [CodeBlock]; the rest is plain text.
 * Under the content there are "Copy" and "Regenerate" actions.
 *
 * @param message The message to show. Expected to have [Author.MENTOR].
 * @param onCopyClick Called when the user taps "Copy".
 * @param onRegenerateClick Called when the user taps "Regenerate".
 * @param isAvatarAnimated `true` while this answer is still being written (streaming, later).
 * @param modifier Modifier applied to the whole row.
 */
@Composable
fun MentorMessageBubble(
    message: ChatMessage,
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    isAvatarAnimated: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val parts = remember(message.text) { splitIntoParts(message.text) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        MentorAvatar(animated = isAvatarAnimated)
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .frostedGlass(
                    c,
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
                        color = c.textPrimary,
                    )
                    is MessagePart.Code -> CodeBlock(code = part.code)
                }
            }
            MentorActions(
                onCopyClick = onCopyClick,
                onRegenerateClick = onRegenerateClick,
            )
        }
    }
}

/**
 * The "Copy" and "Regenerate" actions under the mentor's message.
 *
 * Copy is a text button because material-icons-core has no copy icon.
 */
@Composable
private fun MentorActions(
    onCopyClick: () -> Unit,
    onRegenerateClick: () -> Unit,
) {
    val c = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
            onClick = onCopyClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag(MENTOR_COPY_BUTTON_TAG),
        ) {
            Text(
                text = stringResource(R.string.mentor_message_copy),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
        }
        IconButton(
            onClick = onRegenerateClick,
            modifier = Modifier
                .size(32.dp)
                .testTag(MENTOR_REGENERATE_BUTTON_TAG),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.mentor_message_regenerate),
                tint = c.textSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * A piece of a mentor message: either plain text or a block of code.
 */
private sealed class MessagePart {
    data class Plain(val text: String) : MessagePart()
    data class Code(val code: String) : MessagePart()
}

/**
 * Splits [text] into plain and code parts, using lines that start with ``` as fences.
 *
 * - The opening fence line is dropped whole, so a language name after it (```kotlin) is dropped too.
 * - An opening fence without a closing one makes the rest of the text code.
 * - Blank lines around each part are trimmed, and empty parts are skipped.
 */
private fun splitIntoParts(text: String): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    val currentLines = mutableListOf<String>()
    var insideCode = false

    for (line in text.lines()) {
        if (line.trimStart().startsWith("```")) {
            val part = toPart(currentLines, insideCode)
            if (part != null) parts.add(part)
            currentLines.clear()
            insideCode = !insideCode
        } else {
            currentLines.add(line)
        }
    }

    val lastPart = toPart(currentLines, insideCode)
    if (lastPart != null) parts.add(lastPart)

    return parts
}

/**
 * Builds a part from [lines] without the blank lines around them,
 * or returns `null` if nothing is left.
 */
private fun toPart(lines: List<String>, isCode: Boolean): MessagePart? {
    val content = lines
        .dropWhile { it.isBlank() }
        .dropLastWhile { it.isBlank() }
        .joinToString("\n")
    if (content.isEmpty()) return null
    return if (isCode) MessagePart.Code(content) else MessagePart.Plain(content)
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun MentorMessageBubbleTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorMessageBubble(
            message = previewTextMessage,
            onCopyClick = { },
            onRegenerateClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorMessageBubbleCodePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorMessageBubble(
            message = previewCodeMessage,
            onCopyClick = { },
            onRegenerateClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorMessageBubbleTextAfterCodePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorMessageBubble(
            message = previewTextAfterCodeMessage,
            onCopyClick = { },
            onRegenerateClick = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorActionsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorActions(onCopyClick = { }, onRegenerateClick = { })
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