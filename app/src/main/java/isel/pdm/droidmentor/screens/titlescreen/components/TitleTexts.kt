package isel.pdm.droidmentor.screens.titlescreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/** "Your senior Android mentor", centered and bold. */
@Composable
fun TitleHeadline(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.title_headline),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = LocalAppColors.current.textPrimary,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/** One-line pitch under the headline, at most 280 dp wide. */
@Composable
fun TitleSubtitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.title_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = LocalAppColors.current.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier.widthIn(max = 280.dp),
    )
}

/** Small note under the start button. */
@Composable
fun TitleFootnote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.title_footnote),
        style = MaterialTheme.typography.labelSmall,
        color = LocalAppColors.current.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
private fun TitleTextsPreviewContent() {
    Column(
        modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleHeadline()
        TitleSubtitle()
        TitleFootnote()
    }
}

@Preview(widthDp = 360)
@Composable
private fun TitleTextsDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) { TitleTextsPreviewContent() }
}

@Preview(widthDp = 360)
@Composable
private fun TitleTextsLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) { TitleTextsPreviewContent() }
}