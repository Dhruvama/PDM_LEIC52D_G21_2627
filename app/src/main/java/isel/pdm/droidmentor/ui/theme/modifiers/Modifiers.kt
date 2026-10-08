package isel.pdm.droidmentor.ui.theme.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme


/** Background color with the top and bottom glows drawn over it.
 * @param c The app colors to use.
 */
fun Modifier.glowBackground(c: AppColors) = drawBehind {
    drawRect(c.background)

    drawGlowBlob(c.top.glow.copy(alpha = c.glowAlpha), 0.15f, 0.0f, 1.0f)
    drawGlowBlob(c.bottom.glow.copy(alpha = c.glowAlpha), 0.85f, 1.0f, 1.1f)
}

/** Draws a soft circular glow. x, y and r are fractions of the drawing area. */
private fun DrawScope.drawGlowBlob(color: Color, x: Float, y: Float, r: Float) {
    val center = Offset(size.width * x, size.height * y)
    val radius = size.minDimension * r
    drawCircle(
        brush = Brush.radialGradient(listOf(color, Color.Transparent), center, radius),
        radius = radius,
        center = center
    )
}


/** Translucent "glass" card with a subtle accent border.
 * @param c The app colors to use.
 * @param shape The shape of the card.
 */
fun Modifier.glassCard(
    c: AppColors,
    shape: Shape = RoundedCornerShape(24.dp)
) = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(c.surface.copy(alpha = 0.75f), c.background.copy(alpha = 0.6f))
        )
    )
    .border(
        1.dp,
        Brush.verticalGradient(listOf(c.border, c.borderSubtle)),
        shape
    )

@Preview
@Composable
fun GlowBackgroundDarkPreview() {
    DroidMentorTheme {
        Box(
            modifier = Modifier
                .background(DefaultDarkColors.background)
                .padding(16.dp)
                .glowBackground(DefaultDarkColors)
                .padding(32.dp)
        ) {
            Text("Glow Background Content", color = DefaultDarkColors.textPrimary)
        }
    }
}

@Preview
@Composable
fun GlowBackgroundLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(
            modifier = Modifier
                .background(DefaultLightColors.background)
                .padding(16.dp)
                .glowBackground(DefaultLightColors)
                .padding(32.dp)
        ) {
            Text("Glow Background Content", color = DefaultLightColors.textPrimary)
        }
    }
}

@Preview
@Composable
fun GlassCardPreview() {
    DroidMentorTheme {
        Box(
            modifier = Modifier
                .background(DefaultDarkColors.background)
                .padding(16.dp)
                .glassCard(DefaultDarkColors)
                .padding(32.dp)
        ) {
            Text("Glass Card Content", color = DefaultDarkColors.textPrimary)
        }
    }
}
