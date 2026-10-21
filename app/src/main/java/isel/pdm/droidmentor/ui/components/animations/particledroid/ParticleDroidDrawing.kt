package isel.pdm.droidmentor.ui.components.animations.particledroid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.components.animations.particlelogo.GLOW_SPRITE_PX
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// How the mascot's shadow and each of its dots are drawn.

/** How much (radians) the dots are swept around the center half way through the morph. */
private const val SCATTER_TWIST = 0.7f

// ---- Float ----
private const val FLOAT_PERIOD_SECONDS = 3.6f
private const val FLOAT_AMOUNT = 0.045f

// ---- Sprites ----
private const val SPRITE_BASE_DP = 3f
private const val SPRITE_DEPTH_DP = 4.4f
private const val SPRITE_SCALE = 0.85f
private const val FRONT_DEPTH_THRESHOLD = 0.92f

/**
 * Draws the shadow and every dot [elapsed] seconds after the mascot appeared,
 * with the dots [scatter] of the way from the droid (0) to the swirl (1).
 */
internal fun DrawScope.drawDroid(
    elapsed: Float,
    scatter: Float,
    seeds: DroidSeeds,
    from: FloatArray,
    to: FloatArray,
    sprites: DroidSprites,
    shadowColor: Color,
    additive: Boolean,
) {
    val logoRadius = size.minDimension / 2f
    // 1 while it looks like the droid, 0 while it is the swirl (fades the shadow)
    val droidAmount = 1f - easeInOutCubic(scatter.coerceIn(0f, 1f))

    val floatOffset = sin(elapsed * 2f * PI.toFloat() / FLOAT_PERIOD_SECONDS) * FLOAT_AMOUNT
    val eyeOpen = eyeOpenness(elapsed)

    // Shadow on the "floor", smaller when the head is higher
    val shadowWidth = size.width * (0.42f - floatOffset * 1.3f)
    drawOval(
        shadowColor.copy(alpha = 0.12f * droidAmount),
        topLeft = Offset(center.x - shadowWidth / 2, size.height * 0.81f),
        size = Size(shadowWidth, size.height * 0.05f)
    )

    val blendMode = if (additive) BlendMode.Plus else BlendMode.SrcOver

    for (i in 0 until PARTICLE_COUNT) {
        droidPoint(i, elapsed, eyeOpen, seeds, from)
        swirlPoint(i, elapsed, seeds, to)

        // Later dots start later, so the change "flows" from the center of the head outwards
        val stagger = (scatter * 1.4f - (i.toFloat() / PARTICLE_COUNT) * 0.4f).coerceIn(0f, 1f)
        val progress = easeInOutCubic(stagger)

        val visibility = lerp(from[VISIBILITY], to[VISIBILITY], progress)
        if (visibility < 0.02f) continue
        var x = lerp(from[X], to[X], progress)
        var y = lerp(from[Y], to[Y], progress)
        val depth = lerp(from[DEPTH], to[DEPTH], progress)

        // Swept around the center on the way (same direction as the swirl), so the dots
        // travel along an arc instead of a straight line
        val twist = sin(PI.toFloat() * progress) * SCATTER_TWIST
        if (twist != 0f) {
            val twistCos = cos(twist)
            val twistSin = sin(twist)
            val straightX = x
            x = straightX * twistCos - y * twistSin
            y = straightX * twistSin + y * twistCos
        }
        y += floatOffset

        val sprite = if (depth > FRONT_DEPTH_THRESHOLD) {
            sprites.highlight
        } else {
            // Same direction as the head's crossGradient: top-left → bottom-right
            val g = (((x - y) / 2f + 1f) / 2f).coerceIn(0f, 1f)
            sprites.gradient[(g * (GRADIENT_STEPS - 1)).roundToInt()]
        }
        val spriteSize = ((SPRITE_BASE_DP + depth * SPRITE_DEPTH_DP) * SPRITE_SCALE *
            (0.6f + 0.4f * visibility)).dp.toPx()
        val alpha = ((0.2f + depth * 0.65f) * visibility).coerceAtMost(1f)
        val spriteScale = spriteSize / GLOW_SPRITE_PX

        // Placed with a transform (not whole pixels), so slow dots glide instead of stepping
        withTransform({
            translate(
                left = center.x + x * logoRadius - spriteSize / 2f,
                top = center.y - y * logoRadius - spriteSize / 2f
            )
            scale(spriteScale, spriteScale, pivot = Offset.Zero)
        }) {
            drawImage(image = sprite, alpha = alpha, blendMode = blendMode)
        }
    }
}

/** Slow at the start and at the end, fast in the middle. */
private fun easeInOutCubic(x: Float): Float =
    if (x < 0.5f) 4f * x * x * x
    else {
        val inverse = -2f * x + 2f
        1f - inverse * inverse * inverse / 2f
    }

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction