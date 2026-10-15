package isel.pdm.droidmentor.about.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
 * Speech bubble with the mascot's current tip. When [tip] changes, the old one
 * slides up and fades out while the new one comes in from below.
 *
 * @param tip Text of the tip to show.
 */
@Composable
fun MentorTip(tip: String, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    AnimatedContent(
        targetState = tip,
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 3 }) togetherWith
                    (fadeOut() + slideOutVertically { -it / 3 })
        },
        modifier = modifier,
        label = "tip"
    ) { text ->
        Text(
            text,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .glass(c, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorTipDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorTip(
            tip = stringResource(R.string.about_tip_1),
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorTipLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        MentorTip(
            tip = stringResource(R.string.about_tip_1),
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}