package isel.pdm.droidmentor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground


/**
 * Round send button with the gradient between the two accents and a colored glow.
 * The arrow is drawn by hand, so no icon library is needed.
 *
 * @param onClick the callback to be invoked when the button is clicked
 * @param modifier the modifier to be applied to the button
 * @param diameter the diameter of the button
 * @param contentDescription the content description for accessibility
 */
@Composable
fun SendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp,
    isGlass: Boolean = false,
    contentDescription: String = stringResource(R.string.action_send),
) {
    val colors = LocalAppColors.current
    Box(
        modifier
            .size(diameter)
            .then(
                if (isGlass) Modifier.glass(colors, CircleShape, halo = true)
                else Modifier.solidBubble(colors),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(diameter * 0.38f)) {
            val arrow = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
                lineTo(size.width * 0.25f, size.height / 2f)
                close()
            }
            val iconColor = if (isGlass) colors.accent else colors.onCrossGradient
            drawPath(arrow, iconColor)
        }
    }
}

/** Opaque accent gradient with a colored shadow. */
private fun Modifier.solidBubble(c: AppColors) = this
    .shadow(16.dp, CircleShape, ambientColor = c.accent, spotColor = c.accent)
    .clip(CircleShape)
    .background(c.crossGradient)

@Preview
@Composable
fun SendButtonLargePreview() {
    DroidMentorTheme {
        SendButton(onClick = {})
    }
}

@Preview
@Composable
fun SendButtonDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.size(120.dp).glowBackground(DefaultDarkColors), contentAlignment = Alignment.Center) {
            SendButton(onClick = {})
        }
    }
}

@Preview
@Composable
fun SendButtonLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.size(120.dp).glowBackground(DefaultLightColors), contentAlignment = Alignment.Center) {
            SendButton(onClick = {})
        }
    }
}

@Preview
@Composable
fun SendButtonGlassPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.size(120.dp).glowBackground(DefaultDarkColors), contentAlignment = Alignment.Center) {
            SendButton(onClick = {}, isGlass = true)
        }
    }
}
