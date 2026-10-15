package isel.pdm.droidmentor.chathistory.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
 * Top bar of the Chat History screen: back button and the title in the accent gradient.
 * Pads itself below the status bar and away from display cutouts.
 *
 * @param onBackClick Called when the user taps the back button.
 */
@Composable
fun ChatHistoryTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .frostedGlass(c, CircleShape),) {

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.chat_top_bar_back),
                tint = c.textSecondary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.chat_history_title),
            style = MaterialTheme.typography.headlineSmall.merge(TextStyle(brush = c.crossGradient)),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun ChatHistoryTopBarDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatHistoryTopBar(
            onBackClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current),
        )
    }
}

@Preview
@Composable
private fun ChatHistoryTopBarLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ChatHistoryTopBar(
            onBackClick = { },
            modifier = Modifier.glowBackground(LocalAppColors.current),
        )
    }
}