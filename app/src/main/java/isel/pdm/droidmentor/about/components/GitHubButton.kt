package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * Glass pill button with a halo that opens the project's repository.
 *
 * @param onClick Called when the user taps the button.
 */
@Composable
fun GitHubButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Text(
        stringResource(R.string.about_github_button),
        color = c.accent,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .glass(c, CircleShape, halo = true)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
    )
}

@Preview
@Composable
private fun GitHubButtonDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(24.dp)) {
            GitHubButton(onClick = { })
        }
    }
}

@Preview
@Composable
private fun GitHubButtonLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(24.dp)) {
            GitHubButton(onClick = { })
        }
    }
}