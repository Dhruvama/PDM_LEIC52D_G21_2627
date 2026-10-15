package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * Glass card with a title and the given content stacked below it.
 *
 * @param title Title shown at the top of the card.
 * @param content Content of the card, laid out in a column.
 */
@Composable
fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier
            .frostedGlass(c)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            title,
            color = c.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun InfoCardPreviewContent() {
    val c = LocalAppColors.current
    Column(Modifier.glowBackground(c).padding(16.dp)) {
        InfoCard(stringResource(R.string.about_course), Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about_course_name), color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.about_course_details), color = c.textSecondary)
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun InfoCardDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) { InfoCardPreviewContent() }
}

@Preview(widthDp = 400)
@Composable
private fun InfoCardLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) { InfoCardPreviewContent() }
}