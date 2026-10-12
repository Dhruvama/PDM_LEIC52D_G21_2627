package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

const val EXPAND_CODE_BUTTON_TAG = "expand_code_button"

/**
 * A block of source code inside a message, shown in a monospace font.
 *
 * Long lines do not wrap: the block scrolls horizontally instead. A button in the top-end
 * corner opens the code in an [ExpandedCodeDialog].
 *
 * @param code The code to show, without the surrounding ``` fences.
 * @param modifier Modifier applied to the block.
 */
@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    // Purely visual and local to this block, so it does not belong in the ViewModel's state.
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = CODE_BLOCK_ALPHA),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = false,
            modifier = Modifier
                .padding(end = 32.dp)
                .horizontalScroll(rememberScrollState()),
        )
        IconButton(
            onClick = { isExpanded = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .testTag(EXPAND_CODE_BUTTON_TAG),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_open_in_full),
                contentDescription = stringResource(R.string.code_block_expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (isExpanded) {
        ExpandedCodeDialog(code = code, onDismiss = { isExpanded = false })
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun CodeBlockShortPreview() {
    DroidMentorTheme(dynamicColor = false) {
        CodeBlock(code = previewShortCode)
    }
}

@Preview(showBackground = true)
@Composable
private fun CodeBlockLongLinePreview() {
    DroidMentorTheme(dynamicColor = false) {
        CodeBlock(code = previewLongLineCode)
    }
}

private val previewShortCode = """
    val count by remember { mutableStateOf(0) }
    Text("Count: ${'$'}count")
""".trimIndent()

private val previewLongLineCode = """
    val viewModel = ViewModelProvider(this, ActiveChatScreenViewModelFactory(repository, connectivityObserver)).get(ActiveChatScreenViewModel::class.java)
    viewModel.load()
""".trimIndent()