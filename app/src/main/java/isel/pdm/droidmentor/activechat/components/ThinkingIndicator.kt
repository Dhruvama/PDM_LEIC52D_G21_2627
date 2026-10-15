package isel.pdm.droidmentor.activechat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass

const val THINKING_INDICATOR_TAG = "thinking_indicator"

private const val DOT_FADE_MILLIS = 600
private const val DOT_DELAY_MILLIS = 200

/**
 * Shown while the mentor's reply is pending: the mentor's avatar next to a pill
 * with three "waving" dots and the text "Mentor is thinking…".
 *
 * @param modifier Modifier applied to the whole row.
 */
@Composable
fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(THINKING_INDICATOR_TAG),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MentorAvatar(animated = true)
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .frostedGlass(c, CircleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WavingDots()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.thinking_indicator_text),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
        }
    }
}

/**
 * Three dots that fade in and out one after the other, like a wave.
 */
@Composable
private fun WavingDots() {
    val transition = rememberInfiniteTransition(label = "thinkingDots")

    val firstDotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DOT_FADE_MILLIS),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0),
        ),
        label = "firstDotAlpha",
    )
    val secondDotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DOT_FADE_MILLIS),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(DOT_DELAY_MILLIS),
        ),
        label = "secondDotAlpha",
    )
    val thirdDotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DOT_FADE_MILLIS),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(DOT_DELAY_MILLIS * 2),
        ),
        label = "thirdDotAlpha",
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Dot(alpha = firstDotAlpha)
        Dot(alpha = secondDotAlpha)
        Dot(alpha = thirdDotAlpha)
    }
}

/**
 * One small round dot drawn with the given [alpha].
 */
@Composable
private fun Dot(alpha: Float) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(6.dp)
            .alpha(alpha)
            .background(color = c.accent, shape = CircleShape),
    )
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun ThinkingIndicatorPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ThinkingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun WavingDotsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        WavingDots()
    }
}

@Preview(showBackground = true)
@Composable
private fun DotFullPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Dot(alpha = 1f)
    }
}

@Preview(showBackground = true)
@Composable
private fun DotFadedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Dot(alpha = 0.3f)
    }
}