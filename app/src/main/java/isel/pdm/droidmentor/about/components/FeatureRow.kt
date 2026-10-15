package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * A feature of the app: its label in a small accent pill, followed by a description.
 *
 * @param label Short name of the feature, shown inside the pill.
 * @param description What the feature does.
 */
@Composable
fun FeatureRow(label: String, description: String, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = c.onAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .clip(CircleShape)
                .background(c.accent)
                .padding(horizontal = 10.dp, vertical = 4.dp),

        )
        Text(description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    }
}

@Preview(widthDp = 400)
@Composable
private fun FeatureRowDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        FeatureRow(
            label = stringResource(R.string.about_feature_byok_title),
            description = stringResource(R.string.about_feature_byok_desc),
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun FeatureRowLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        FeatureRow(
            label = stringResource(R.string.about_feature_byok_title),
            description = stringResource(R.string.about_feature_byok_desc),
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}