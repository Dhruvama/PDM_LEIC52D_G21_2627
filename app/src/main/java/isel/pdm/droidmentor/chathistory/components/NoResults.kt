package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val NO_RESULTS_TAG = "no_results"

/**
 * Shown when there are saved chats but none matches the search.
 *
 * @param query The search text, quoted in the message.
 */
@Composable
fun NoResults(query: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(32.dp)
            .testTag(NO_RESULTS_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.chat_history_no_results, query),
            color = LocalAppColors.current.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 400, heightDp = 300)
@Composable
private fun NoResultsDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        NoResults(
            query = "elden ring",
            modifier = Modifier.fillMaxSize().glowBackground(LocalAppColors.current),
        )
    }
}

@Preview(widthDp = 400, heightDp = 300)
@Composable
private fun NoResultsLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        NoResults(
            query = "elden ring",
            modifier = Modifier.fillMaxSize().glowBackground(LocalAppColors.current),
        )
    }
}