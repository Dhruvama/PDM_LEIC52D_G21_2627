package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.background
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
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.ui.theme.modifiers.layeredGlass

const val CHAT_SEARCH_FIELD_TAG = "chat_search_field"
const val CHAT_SEARCH_MATCH_COUNT_TAG = "chat_search_match_count"

/**
 * Glass pill to search the saved chats, with the number of matches and a button to clear it
 * once something is typed.
 *
 * @param query Text currently typed.
 * @param onQueryChange Called with the new text whenever the user types or clears it.
 * @param matchCount How many chats match [query]; shown only while [query] isn't blank.
 */
@Composable
fun ChatSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    matchCount: Int = 0,
) {
    val c = LocalAppColors.current
    val keyboard = LocalSoftwareKeyboardController.current
    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = modifier
            .layeredGlass(c, CircleShape)
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
        if (query.isNotBlank()) {
            MatchCountBadge(count = matchCount)
        }
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

/** Small accent pill with how many chats match the search. */
@Composable
private fun MatchCountBadge(count: Int, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .background(c.accent.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(CHAT_SEARCH_MATCH_COUNT_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = c.accent,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

/** Draws the field over the app's background so the glass is visible. */
@Composable
private fun PreviewContent(query: String, matchCount: Int = 0, colors: AppColors = DefaultDarkColors) {
    DroidMentorTheme(colors = colors) {
        Box(Modifier.glowBackground(LocalAppColors.current)) {
            ChatSearchField(
                query = query,
                onQueryChange = { },
                matchCount = matchCount,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun ChatSearchFieldEmptyPreview() = PreviewContent(query = "")

@Preview(widthDp = 360)
@Composable
private fun ChatSearchFieldTypedPreview() = PreviewContent(query = "compose", matchCount = 2)

@Preview(widthDp = 360)
@Composable
private fun ChatSearchFieldNoMatchesPreview() = PreviewContent(query = "elden ring", matchCount = 0)

@Preview(widthDp = 360)
@Composable
private fun ChatSearchFieldEmptyLightPreview() = PreviewContent(query = "", colors = DefaultLightColors)

@Preview(widthDp = 360)
@Composable
private fun ChatSearchFieldTypedLightPreview() =
    PreviewContent(query = "compose", matchCount = 2, colors = DefaultLightColors)