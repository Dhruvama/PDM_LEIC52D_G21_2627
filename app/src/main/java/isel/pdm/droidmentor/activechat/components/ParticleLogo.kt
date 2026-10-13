package isel.pdm.droidmentor.activechat.components

import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas as BitmapCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.readableOn
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

const val PARTICLE_LOGO_TAG = "particle_logo"

private const val HOLD_SECONDS = 3.4f
private const val MORPH_SECONDS = 1.8f
private const val STAGE_SECONDS = HOLD_SECONDS + MORPH_SECONDS
private const val CYCLE_SECONDS = STAGE_SECONDS * 3

private const val SHAPE_SWARM = 0
private const val SHAPE_SPHERE = 1
private const val SHAPE_HELIX = 2
private const val SHAPE_ENTRY_CLOUD = 3

/** How long the entry swirl keeps turning before it gathers: it covers the cross-fade from the Title screen. */
private const val ENTRY_HOLD = 0.5f

/** How long the entry swirl takes to gather into the sphere. */
private const val ENTRY_DURATION = 1.2f

/** When the entry is over and the normal loop takes over. */
private const val ENTRY_END = ENTRY_HOLD + ENTRY_DURATION

/** Loop time at which the sphere hold begins (after the swarm hold and the swarm → sphere morph). */
private const val SPHERE_HOLD_START = HOLD_SECONDS + MORPH_SECONDS

// The Title screen's mascot is drawn in a 300×330 dp box, so its swirl has a radius of
// min(300, 330) / 2 = 150 dp. The entry swirl has that same size on screen.
private const val TITLE_SWIRL_RADIUS_DP = 150f

private const val HELIX_LEVELS = 30
private const val HELIX_SLOTS_PER_LEVEL = 6
private const val HELIX_SLOTS = HELIX_LEVELS * HELIX_SLOTS_PER_LEVEL

private val GOLDEN_ANGLE = (PI * (3.0 - sqrt(5.0))).toFloat()

/** Time drawn when the user turned animations off: the middle of the sphere's hold. */
private const val REDUCED_MOTION_TIME_SECONDS = 7f

private const val SWARM_RANDOM_SEED = 42

/** Width and height (px) of the glow sprite bitmap. */
private const val GLOW_SPRITE_PX = 32

/** Sprite size (dp) of a dot at the back (depth 0). The visible core is about a third of it. */
private const val SPRITE_BASE_DP = 2.6f

/** Extra sprite size (dp) added at the front (depth 1). */
private const val SPRITE_DEPTH_DP = 4.6f

/** Overall scale applied to the sprite size. */
private const val SPRITE_SCALE = 0.75f

/** Dots deeper than this (closest to the viewer) use the front color instead of an accent. */
private const val FRONT_DEPTH_THRESHOLD = 0.9f

/** Colors of the particle logo. Chosen by the user in Settings (built by a teammate); null = use the theme. */
data class ParticleLogoColors(
    val main: Color,       // most dots
    val second: Color,     // every 3rd dot
    val highlight: Color,  // dots in front (highest depth)
)

// Indexes inside the scratch FloatArray(4) written by the shape functions.
private const val X = 0
private const val Y = 1
private const val DEPTH = 2
private const val VISIBILITY = 3

/**
 * Decorative logo made of a cloud of small dots that loops through three shapes,
 * morphing smoothly between them: a drifting swarm, a rotating sphere and a rotating
 * dotted double helix. Dots "in front" are bigger and more opaque.
 *
 * Only the draw phase repeats every frame; the composable itself does not recompose
 * while animating. If the user turned animations off in the system settings, the
 * sphere is drawn without motion.
 *
 * @param modifier Modifier applied to the drawing area.
 * @param size Width and height of the drawing area.
 * @param particleCount Number of dots.
 * @param glow `true` to draw the dots with additive blending, so overlapping glows add up.
 * Only looks good on dark backgrounds.
 * @param logoColors Colors chosen by the user, or `null` to use the theme's accents.
 * @param assembleOnStart `true` to start as the Title screen's swirl, keep it turning for
 * [ENTRY_HOLD] seconds (while the screens cross-fade), gather it into the sphere during the
 * next [ENTRY_DURATION] seconds, then continue the loop from the sphere.
 * Used for the entry transition from the Title screen. Ignored when [staticTimeSeconds] is set
 * or animations are turned off.
 * @param staticTimeSeconds When not `null`, nothing animates and the logo is drawn at this
 * instant of the loop (useful for previews).
 */
@Composable
fun ParticleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    particleCount: Int = 450,
    glow: Boolean = false,
    logoColors: ParticleLogoColors? = null,
    assembleOnStart: Boolean = false,
    staticTimeSeconds: Float? = null,
) {
    val context = LocalContext.current
    val animatorScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    var frozenTimeSeconds = staticTimeSeconds
    // Reduced motion: animations are turned off in the system settings, so show the sphere still.
    if (frozenTimeSeconds == null && animatorScale == 0f) {
        frozenTimeSeconds = REDUCED_MOTION_TIME_SECONDS
    }
    // A frozen logo (static preview or reduced motion) never plays the entry.
    val playEntry = assembleOnStart && frozenTimeSeconds == null

    val seeds = remember(particleCount) { SwarmSeeds(particleCount) }
    val from = remember { FloatArray(4) }
    val to = remember { FloatArray(4) }

    var timeSeconds by remember { mutableFloatStateOf(0f) }
    var swirlTimeSeconds by remember { mutableFloatStateOf(0f) }
    if (frozenTimeSeconds == null) {
        LaunchedEffect(Unit) {
            val startNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameNanos ->
                    timeSeconds = (frameNanos - startNanos) / 1_000_000_000f
                    swirlTimeSeconds = swirlClockSeconds(frameNanos)
                }
            }
        }
    }

    // The entry swirl must have the same size on screen as the Title screen's swirl,
    // whatever the size of this logo.
    val entryCloudScale = TITLE_SWIRL_RADIUS_DP / (size.value / 2f)

    val c = LocalAppColors.current
    val mainColor: Color
    val secondColor: Color
    val highlightColor: Color
    if (logoColors == null) {
        mainColor = c.topAccent
        secondColor = c.bottomAccent
        highlightColor = c.textPrimary
    } else {
        // A color the user picks may be invisible on the background (e.g. dark on dark),
        // so the main and second colors are adjusted until they are readable on it.
        mainColor = logoColors.main.readableOn(c.background)
        secondColor = logoColors.second.readableOn(c.background)
        highlightColor = logoColors.highlight
    }

    // Created once (and again only if a color changes), never per frame.
    val sprites = remember(mainColor, secondColor, highlightColor) {
        GlowSprites(
            main = makeGlowSprite(mainColor),
            second = makeGlowSprite(secondColor),
            highlight = makeGlowSprite(highlightColor),
        )
    }

    // No clip on purpose: the entry cloud is bigger than the logo and must draw outside its bounds.
    Canvas(
        modifier = modifier
            .size(size)
            .testTag(PARTICLE_LOGO_TAG),
    ) {
        // timeSeconds is read ONLY here, inside the draw lambda: each new frame only
        // repeats the draw phase, without recomposing or re-measuring anything.
        val elapsed = frozenTimeSeconds ?: timeSeconds
        drawParticles(
            elapsed = elapsed,
            swirlTime = frozenTimeSeconds ?: swirlTimeSeconds,
            assembleOnStart = playEntry,
            particleCount = particleCount,
            seeds = seeds,
            from = from,
            to = to,
            entryCloudScale = entryCloudScale,
            sprites = sprites,
            glow = glow,
        )
    }
}

/** One glow sprite per logo color: [main] for most dots, [second] for every 3rd, [highlight] for the front. */
private class GlowSprites(
    val main: ImageBitmap,
    val second: ImageBitmap,
    val highlight: ImageBitmap,
)

/**
 * Builds a soft round dot of [color]: solid in the center, fading to transparent at the edge.
 * Drawn once into a bitmap, so each frame only copies it instead of computing a gradient.
 */
private fun makeGlowSprite(color: Color, sizePx: Int = GLOW_SPRITE_PX): ImageBitmap {
    val bitmap = ImageBitmap(sizePx, sizePx)
    val canvas = BitmapCanvas(bitmap)
    val center = Offset(sizePx / 2f, sizePx / 2f)
    val paint = Paint()
    paint.shader = RadialGradientShader(
        center = center,
        radius = sizePx / 2f,
        colors = listOf(color, color.copy(alpha = 0.55f), Color.Transparent),
        colorStops = listOf(0f, 0.35f, 1f),
    )
    canvas.drawCircle(center = center, radius = sizePx / 2f, paint = paint)
    return bitmap
}

/**
 * Random values for the swarm shape and for the entry cloud, created once per particle
 * count with a fixed seed, so both look the same every time and nothing is allocated per frame.
 */
private class SwarmSeeds(particleCount: Int) {
    val diskX = FloatArray(particleCount)
    val diskY = FloatArray(particleCount)
    val phase = FloatArray(particleCount)
    val speed = FloatArray(particleCount)

    val entryAngle = FloatArray(particleCount)
    val entryRadius = FloatArray(particleCount)
    val entryPhase = FloatArray(particleCount)
    val entrySpeed = FloatArray(particleCount)

    val swirlRadius = FloatArray(particleCount)
    val swirlJitter = FloatArray(particleCount)

    init {
        val random = Random(SWARM_RANDOM_SEED)
        for (i in 0 until particleCount) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val radius = random.nextFloat().pow(0.6f) * 0.95f
            diskX[i] = cos(angle) * radius
            diskY[i] = sin(angle) * radius
            phase[i] = random.nextFloat() * 2f * PI.toFloat()
            speed[i] = 0.4f + random.nextFloat() * 0.6f
        }
        // In a second loop, so the swarm keeps exactly the same random values as before.
        for (i in 0 until particleCount) {
            entryAngle[i] = random.nextFloat() * 2f * PI.toFloat()
            entryRadius[i] = 0.6f + random.nextFloat() * 0.9f
            entryPhase[i] = random.nextFloat() * 2f * PI.toFloat()
            entrySpeed[i] = 0.5f + random.nextFloat()
        }
        // In a third loop, so the swarm and entry values stay the same as before.
        for (i in 0 until particleCount) {
            swirlRadius[i] = 0.12f + random.nextFloat().pow(0.7f) * 0.83f
            swirlJitter[i] = (random.nextFloat() - 0.5f) * 0.5f
        }
    }
}

/**
 * Draws every particle [elapsed] seconds after the logo appeared: finds which shapes are
 * being held or morphed from/to, mixes both positions per particle and draws the dot.
 *
 * When [assembleOnStart] is `true`, the entry swirl turns for [ENTRY_HOLD] seconds, then morphs
 * into the sphere during [ENTRY_DURATION] seconds, and the loop continues from the start of the
 * sphere hold.
 */
private fun DrawScope.drawParticles(
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

/**
 * Writes into [out] where particle [i] is in the given [shape] at time [t].
 */
private fun shapePoint(
    shape: Int,
    i: Int,
    particleCount: Int,
    t: Float,
    seeds: SwarmSeeds,
    entryCloudScale: Float,
    out: FloatArray,
) {
    when (shape) {
        SHAPE_SWARM -> swarmPoint(i, t, seeds, out)
        SHAPE_SPHERE -> spherePoint(i, particleCount, t, out)
        SHAPE_HELIX -> helixPoint(i, t, out)
        SHAPE_ENTRY_CLOUD -> entryCloudPoint(i, t, seeds, entryCloudScale, out)
    }
}

/**
 * Clock of the entry swirl, from a frame time of `withFrameNanos`. The Title screen turns its
 * swirl with this same clock, so both swirls line up while the screens cross-fade. It wraps
 * around after 1000 s so the float value stays precise.
 */
private fun swirlClockSeconds(frameNanos: Long): Float =
    ((frameNanos / 1_000_000L) % 1_000_000L) / 1000f

/**
 * Entry swirl, used ONLY for the entry transition: the same swirl the Title screen's mascot
 * ends with (three spiral arms rotating around the center, brighter in the core), scaled by
 * [entryCloudScale] to match its size on screen.
 */
private fun entryCloudPoint(
    i: Int,
    t: Float,
    seeds: SwarmSeeds,
    entryCloudScale: Float,
    out: FloatArray,
) {
    val r = seeds.swirlRadius[i]
    val arm = (i % 3) * (2f * PI.toFloat() / 3f)
    val angle = arm + r * 3.6f + t * 0.7f + seeds.swirlJitter[i]
    out[X] = cos(angle) * r * entryCloudScale
    out[Y] = sin(angle) * r * 0.8f * entryCloudScale
    out[DEPTH] = (0.25f + 0.65f * (1f - r) + 0.15f * sin(t * 2f + seeds.entryPhase[i])).coerceIn(0f, 1f)
    out[VISIBILITY] = 1f
}

/**
 * Swarm: particle [i] on a ring with an empty center, drifting slightly around its seed position.
 * Uses the same seeds as the entry cloud.
 */
private fun swarmPoint(i: Int, t: Float, seeds: SwarmSeeds, out: FloatArray) {
    val angle = seeds.entryAngle[i]
    // entryRadius is 0.6..1.5, so the dots sit between ~0.38 and 0.95.
    val radius = seeds.entryRadius[i] / 1.5f * 0.95f
    val phase = seeds.entryPhase[i]
    val speed = seeds.entrySpeed[i]
    out[X] = cos(angle) * radius + sin(t * speed + phase) * 0.08f
    out[Y] = sin(angle) * radius + cos(t * speed * 0.8f + phase) * 0.08f
    out[DEPTH] = 0.35f + 0.3f * sin(t + phase)
    out[VISIBILITY] = 1f
}

/**
 * Sphere: particle [i] on a Fibonacci sphere rotating around the vertical axis,
 * projected to 2D. The depth comes from how close the point is to the viewer.
 */
private fun spherePoint(i: Int, particleCount: Int, t: Float, out: FloatArray) {
    val lastIndex = (particleCount - 1).coerceAtLeast(1)
    val y = 1f - (i.toFloat() / lastIndex) * 2f
    val r = sqrt(1f - y * y)
    val a = i * GOLDEN_ANGLE + t * 0.6f
    val x = cos(a) * r
    val z = sin(a) * r
    out[X] = x * 0.82f
    out[Y] = y * 0.82f
    out[DEPTH] = (z + 1f) / 2f
    out[VISIBILITY] = 1f
}

/**
 * Dotted double helix: 30 levels with 6 slots each. Slot 0 is on strand A, slot 1 on
 * strand B and slots 2..5 are smaller "rung" dots between them. Particles beyond the
 * 180 slots are invisible here (they fade out and come back in the swarm).
 */
private fun helixPoint(i: Int, t: Float, out: FloatArray) {
    val slot = i % HELIX_SLOTS
    val level = slot / HELIX_SLOTS_PER_LEVEL
    val part = slot % HELIX_SLOTS_PER_LEVEL

    val k = level / (HELIX_LEVELS - 1f)
    val y = (k * 2f - 1f) * 0.88f
    val a = k * PI.toFloat() * 4f + t * 1.4f

    val strandAX = sin(a) * 0.45f
    val strandADepth = (cos(a) + 1f) / 2f
    val strandBX = -strandAX
    val strandBDepth = 1f - strandADepth

    out[Y] = y
    if (part == 0) {
        out[X] = strandAX
        out[DEPTH] = strandADepth
        out[VISIBILITY] = 1f
    } else if (part == 1) {
        out[X] = strandBX
        out[DEPTH] = strandBDepth
        out[VISIBILITY] = 1f
    } else {
        val f = (part - 1) / 5f
        out[X] = lerp(strandAX, strandBX, f)
        out[DEPTH] = lerp(strandADepth, strandBDepth, f)
        out[VISIBILITY] = 0.55f
    }

    if (i >= HELIX_SLOTS) {
        out[VISIBILITY] = 0f
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

/** Value between [start] (at fraction 0) and [stop] (at fraction 1). */
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun ParticleLogoSwarmPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogo(staticTimeSeconds = 1f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoSpherePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogo(staticTimeSeconds = 7f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoHelixPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogo(staticTimeSeconds = 12f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoAnimatedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogo()
    }
}

// Animated: check it in Interactive Mode (restart it to replay the entry).
@Preview(showBackground = true)
@Composable
private fun ParticleLogoAssembleOnStartPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(
            modifier = Modifier.size(width = 360.dp, height = 640.dp),
            contentAlignment = Alignment.Center,
        ) {
            ParticleLogo(
                assembleOnStart = true,
                logoColors = ParticleLogoColors(
                    main = PresetThemes[2].topAccent,      // laranja
                    second = PresetThemes[2].bottomAccent, // rosa
                    highlight = PresetThemes[1].textPrimary,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoGlowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.inverseSurface)) {
            ParticleLogo(glow = true, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoSphereDarkGlowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogo(glow = true, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoSphereLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogo(glow = false, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoCustomColorsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogo(
                glow = true,
                logoColors = previewCustomColors,
                staticTimeSeconds = 7f,
            )
        }
    }
}

@Preview
@Composable
private fun ParticleLogoUnreadableColorFixedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogo(
                glow = true,
                logoColors = previewUnreadableColors,
                staticTimeSeconds = 7f,
            )
        }
    }
}

private val previewCustomColors = ParticleLogoColors(
    main = PresetThemes[2].topAccent,
    second = PresetThemes[2].bottomAccent,
    highlight = DefaultDarkColors.textPrimary,
)

// main is the background color itself: readableOn must lighten it so the dots still show.
private val previewUnreadableColors = ParticleLogoColors(
    main = DefaultDarkColors.background,
    second = DefaultDarkColors.bottomAccent,
    highlight = DefaultDarkColors.textPrimary,
)
