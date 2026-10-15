package isel.pdm.droidmentor.about.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

/**
 * Top bar of the About screen: a round glass back button, the same 44.dp size
 * as the one in the chat's top bar.
 *
 * @param onBackClick Called when the user taps the back button.
 */
@Composable
fun AboutTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .frostedGlass(c, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.about_back_content_description),
                tint = c.textPrimary
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun AboutTopBarDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        AboutTopBar(
            onBackClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun AboutTopBarLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        AboutTopBar(
            onBackClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}