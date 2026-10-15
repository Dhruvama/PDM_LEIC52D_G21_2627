package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass

const val CHAT_SEARCH_FIELD_TAG = "chat_search_field"

/**
 * Glass pill to search the saved chats, with a button to clear it once something is typed.
 *
 * @param query Text currently typed.
 * @param onQueryChange Called with the new text whenever the user types or clears it.
 */
@Composable
fun ChatSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val keyboard = LocalSoftwareKeyboardController.current
    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = modifier
            .glass(c, CircleShape)
            .padding(start = 16.dp, end = 4.dp)
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = c.textSecondary)
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = textStyle.copy(color = c.textPrimary),
            cursorBrush = SolidColor(c.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 14.dp)
                .testTag(CHAT_SEARCH_FIELD_TAG),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.chat_history_search_hint),
                            style = textStyle,
                            color = c.textSecondary,
                        )
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.chat_history_clear_search),
                    tint = c.textSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatSearchFieldEmptyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatSearchField(query = "", onQueryChange = { }, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatSearchFieldTypedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatSearchField(query = "compose", onQueryChange = { }, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatSearchFieldEmptyLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ChatSearchField(query = "", onQueryChange = { }, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatSearchFieldTypedLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ChatSearchField(query = "compose", onQueryChange = { }, modifier = Modifier.padding(16.dp))
    }
}