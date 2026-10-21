package isel.pdm.droidmentor.activechat.components.particlelogo

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// The loop timeline of the particle logo, and how each dot is drawn.

private const val HOLD_SECONDS = 3.4f
private const val MORPH_SECONDS = 1.8f
private const val STAGE_SECONDS = HOLD_SECONDS + MORPH_SECONDS
private const val CYCLE_SECONDS = STAGE_SECONDS * 3

/** How long the entry swirl keeps turning before it gathers: it covers the cross-fade from the Title screen. */
internal const val ENTRY_HOLD = 0.5f

/** How long the entry swirl takes to gather into the sphere. */
internal const val ENTRY_DURATION = 1.2f

/** When the entry is over and the normal loop takes over. */
private const val ENTRY_END = ENTRY_HOLD + ENTRY_DURATION

/** Loop time at which the sphere hold begins (after the swarm hold and the swarm → sphere morph). */
private const val SPHERE_HOLD_START = HOLD_SECONDS + MORPH_SECONDS

/** Sprite size (dp) of a dot at the back (depth 0). The visible core is about a third of it. */
private const val SPRITE_BASE_DP = 2.6f

/** Extra sprite size (dp) added at the front (depth 1). */
private const val SPRITE_DEPTH_DP = 4.6f

/** Overall scale applied to the sprite size. */
private const val SPRITE_SCALE = 0.75f

/** Dots deeper than this (closest to the viewer) use the front color instead of an accent. */
private const val FRONT_DEPTH_THRESHOLD = 0.9f

/**
 * Draws every particle [elapsed] seconds after the logo appeared: finds which shapes are
 * being held or morphed from/to, mixes both positions per particle and draws the dot.
 *
 * When [assembleOnStart] is `true`, the entry swirl turns for [ENTRY_HOLD] seconds, then morphs
 * into the sphere during [ENTRY_DURATION] seconds, and the loop continues from the start of the
 * sphere hold.
 */
internal fun DrawScope.drawParticles(
    elapsed: Float,
    swirlTime: Float,
    assembleOnStart: Boolean,
    particleCount: Int,
    seeds: SwarmSeeds,
    from: FloatArray,
    to: FloatArray,
    entryCloudScale: Float,
    sprites: GlowSprites,
    glow: Boolean,
) {
    val logoRadius = size.minDimension / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    // With the entry, the loop is shifted so that, when the entry ends, it is exactly at the
    // start of the sphere hold. The entry also rotates its sphere with this time, so there is no jump.
    val loopTime = if (assembleOnStart) {
        elapsed - ENTRY_END + SPHERE_HOLD_START
    } else {
        elapsed
    }

    val fromShape: Int
    val toShape: Int
    val morphProgress: Float
    if (assembleOnStart && elapsed < ENTRY_END) {
        // Entry: the Title screen's swirl keeps turning, then gathers into the sphere.
        fromShape = SHAPE_ENTRY_CLOUD
        toShape = SHAPE_SPHERE
        morphProgress = ((elapsed - ENTRY_HOLD) / ENTRY_DURATION).coerceIn(0f, 1f)
    } else {
        // Stage 0: swarm → sphere, stage 1: sphere → helix, stage 2: helix → swarm.
        // Each stage holds its first shape for HOLD_SECONDS, then morphs for MORPH_SECONDS.
        val cycleTime = loopTime % CYCLE_SECONDS
        val stage = (cycleTime / STAGE_SECONDS).toInt().coerceAtMost(2)
        val timeInStage = cycleTime - stage * STAGE_SECONDS
        fromShape = stage
        toShape = (stage + 1) % 3
        morphProgress = ((timeInStage - HOLD_SECONDS) / MORPH_SECONDS).coerceIn(0f, 1f)
    }

    val blendMode = if (glow) BlendMode.Plus else BlendMode.SrcOver

    for (i in 0 until particleCount) {
        // The entry swirl turns with the same clock as the Title screen's, so both line up.
        val fromTime = if (fromShape == SHAPE_ENTRY_CLOUD) swirlTime else loopTime
        shapePoint(fromShape, i, particleCount, fromTime, seeds, entryCloudScale, from)
        shapePoint(toShape, i, particleCount, loopTime, seeds, entryCloudScale, to)

        // Later particles start morphing a bit later, so the change "flows" through the cloud.
        val stagger = (morphProgress * 1.4f - (i.toFloat() / particleCount) * 0.4f).coerceIn(0f, 1f)
        val progress = easeInOutCubic(stagger)

        val visibility = lerp(from[VISIBILITY], to[VISIBILITY], progress)
        if (visibility < 0.02f) continue
        val x = lerp(from[X], to[X], progress)
        val y = lerp(from[Y], to[Y], progress)
        val depth = lerp(from[DEPTH], to[DEPTH], progress)

        // Size of the whole glow; the visible core is about a third of it.
        val spriteSize = ((SPRITE_BASE_DP + depth * SPRITE_DEPTH_DP) * SPRITE_SCALE *
            (0.6f + 0.4f * visibility)).dp.toPx().roundToInt().coerceAtLeast(1)
        val alpha = (0.15f + depth * 0.6f) * visibility
        val sprite = if (depth > FRONT_DEPTH_THRESHOLD) {
            sprites.highlight
        } else if (i % 3 == 0) {
            sprites.second
        } else {
            sprites.main
        }
        val dotX = (centerX + x * logoRadius).roundToInt()
        val dotY = (centerY - y * logoRadius).roundToInt()

        // IntOffset and IntSize are value classes (packed in a Long), so this does not allocate.
        drawImage(
            image = sprite,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(GLOW_SPRITE_PX, GLOW_SPRITE_PX),
            dstOffset = IntOffset(dotX - spriteSize / 2, dotY - spriteSize / 2),
            dstSize = IntSize(spriteSize, spriteSize),
            alpha = alpha,
            blendMode = blendMode,
        )
    }
}

/** Slow at the start and at the end, fast in the middle. */
private fun easeInOutCubic(x: Float): Float {
    return if (x < 0.5f) {
        4f * x * x * x
    } else {
        val inverse = -2f * x + 2f
        1f - inverse * inverse * inverse / 2f
    }
}
