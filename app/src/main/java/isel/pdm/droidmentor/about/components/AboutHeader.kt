package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * App name in the accent gradient, tagline and version.
 *
 * @param version App version, shown as "v<version>" in a glass pill.
 */
@Composable
fun AboutHeader(version: String, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "DroidMentor",
            style = MaterialTheme.typography.displaySmall.merge(TextStyle(brush = c.crossGradient)),
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.about_header_subtitle),
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            "v$version",
            style = MaterialTheme.typography.labelMedium,
            color = c.accent,
            modifier = Modifier
                .glass(c, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun AboutHeaderDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        AboutHeader(
            version = "1.0",
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun AboutHeaderLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        AboutHeader(
            version = "1.0",
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}