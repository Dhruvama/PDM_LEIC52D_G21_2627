package isel.pdm.droidmentor.ui.components.animations.particledroid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import isel.pdm.droidmentor.ui.components.animations.particlelogo.makeGlowSprite
import isel.pdm.droidmentor.ui.theme.LocalAppColors

/** How many steps the head's gradient (bottom accent → top accent) is split into. */
internal const val GRADIENT_STEPS = 12

/** One sprite per step of the head's gradient, plus one for the dots in front. */
internal class DroidSprites(val gradient: Array<ImageBitmap>, val highlight: ImageBitmap)

/**
 * The glow sprites in the theme's colors. Created once (and again only if a color changes),
 * never per frame. Holds no UI, so it has no preview.
 */
@Composable
internal fun rememberDroidSprites(): DroidSprites {
    val c = LocalAppColors.current
    return remember(c.bottomAccent, c.topAccent, c.textPrimary) {
        DroidSprites(
            gradient = Array(GRADIENT_STEPS) {
                makeGlowSprite(lerp(c.bottomAccent, c.topAccent, it / (GRADIENT_STEPS - 1f)))
            },
            highlight = makeGlowSprite(c.textPrimary),
        )
    }
}