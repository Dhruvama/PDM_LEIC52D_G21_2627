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
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val EXPAND_CODE_BUTTON_TAG = "expand_code_button"

/**
 * A block of source code inside a message, shown in a monospace font.
 *
 * Long lines do not wrap: the block scrolls horizontally instead. A button in the top-end
 * corner opens the code in an [ExpandedCodeDialogComponent].
 *
 * @param code The code to show, without the surrounding fences.
 * @param modifier applied to the block.
 */
@Composable
fun CodeBlockComponent(code: String, modifier: Modifier = Modifier) {
    // Purely visual and local to this block, so it does not belong in the ViewModel's state.
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val c = LocalAppColors.current

    Box(
        modifier = modifier
            .background(
                color = c.background.copy(alpha = CODE_BLOCK_ALPHA),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = c.textPrimary,
            softWrap = false,
            modifier = Modifier
                .padding(end = 32.dp)
                .horizontalScroll(rememberScrollState()),
        )
        ComponentExpandButton(
            onExpandClick = { isExpanded = true },
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }

    if (isExpanded) {
        ExpandedCodeDialogComponent(code = code, onDismiss = { isExpanded = false })
    }
}

/**
 * Small 32.dp icon button that opens the code block in an [ExpandedCodeDialogComponent].
 *
 * @param onExpandClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
fun ComponentExpandButton(onExpandClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onExpandClick,
        modifier = modifier
            .size(32.dp)
            .testTag(EXPAND_CODE_BUTTON_TAG),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_full),
            contentDescription = stringResource(R.string.code_block_expand),
            tint = LocalAppColors.current.textSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(widthDp = 360)
@Composable
private fun CodeBlockComponentShortPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            CodeBlockComponent(code = previewShortCode)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun CodeBlockComponentShortLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            CodeBlockComponent(code = previewShortCode)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun CodeBlockComponentLongLinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            CodeBlockComponent(code = previewLongLineCode)
        }
    }
}

@Preview
@Composable
private fun ComponentExpandButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentExpandButton(onExpandClick = { })
        }
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