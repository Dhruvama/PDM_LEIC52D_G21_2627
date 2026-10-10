package isel.pdm.droidmentor.ui.theme.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
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

/**
 * Translucent glass surface for any shape: a faint tint of the two accents, a white sheen
 * on the top half and a light rim. Works for buttons, text fields, cards, chips...
 * Content placed inside should use [AppColors.accent] or [AppColors.textPrimary].
 *
 * An elevation shadow is avoided on purpose, because it would show through the
 * translucent fill and look muddy; use [halo] for a soft accent glow instead.
 *
 * @param c The app colors to use.
 * @param shape The shape of the surface.
 * @param halo Whether to draw a soft accent glow around it (good for primary buttons).
 */
fun Modifier.glass(
    c: AppColors,
    shape: Shape = RoundedCornerShape(24.dp),
    halo: Boolean = false,
) = this
    .then(
        if (halo) Modifier.dropShadow(
            shape,
            Shadow(radius = 12.dp, color = c.accent.copy(alpha = 0.35f))
        )
        else Modifier
    )
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(c.bottomAccent.copy(alpha = 0.22f), c.topAccent.copy(alpha = 0.10f))
        )
    )
    .background(c.surface.copy(alpha = if (c.isLight) 0.45f else 0.25f))
    .background(
        Brush.verticalGradient(
            0f to Color.White.copy(alpha = if (c.isLight) 0.55f else 0.12f),
            0.5f to Color.Transparent
        )
    )
    .border(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (c.isLight) 0.8f else 0.35f),
                c.accent.copy(alpha = 0.25f)
            )
        ),
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
                .glowBackground(DefaultDarkColors)
                .padding(16.dp)
                .glassCard(DefaultDarkColors)
                .padding(32.dp)
        ) {
            Text("Glass Card Content", color = DefaultDarkColors.textPrimary)
        }
    }
}

/** The [glass] modifier on a card, a text field and a button, over the glow background. */
@Composable
private fun GlassPreviewContent(c: AppColors) {
    DroidMentorTheme(colors = c) {
        Column(
            Modifier
                .width(320.dp)
                .glowBackground(c)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .glass(c)
                    .padding(20.dp)
            ) {
                Text("Glass Card Content", color = c.textPrimary)
            }

            var text by remember { mutableStateOf("") }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(c, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { inner ->
                    if (text.isEmpty()) Text("Escreve uma mensagem...", color = c.textSecondary)
                    inner()
                }
            )

            Box(
                Modifier
                    .glass(c, CircleShape, halo = true)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Glass Button", color = c.accent)
            }
        }
    }
}

@Preview
@Composable
fun GlassDarkPreview() = GlassPreviewContent(DefaultDarkColors)

@Preview
@Composable
fun GlassLightPreview() = GlassPreviewContent(DefaultLightColors)

