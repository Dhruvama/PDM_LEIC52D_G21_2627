package isel.pdm.droidmentor.about

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Canvas as BitmapCanvas

// ---- Particles ----
private const val PARTICLE_COUNT = 420
private const val ANTENNA_DOTS = 14
private const val HEAD_DOTS = PARTICLE_COUNT - 2 * ANTENNA_DOTS

// ---- Loop: the droid holds, scatters into a swirl and comes back ----
private const val DROID_HOLD_MILLIS = 6_000L
private const val SWIRL_HOLD_MILLIS = 2_600L

/** How long the swirl stays after a tap, before the droid gathers back with its new tip. */
private const val TAP_SWIRL_HOLD_MILLIS = 500L

// ---- Scatter: 0 = droid, 1 = swirl ----
private const val GATHERED = 0f
private const val SCATTERED = 1f

/**
 * A spring instead of a fixed duration: it keeps the current speed when a tap interrupts
 * the morph half way, so the dots never jump or stop dead.
 */
private val MORPH_SPEC = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 30f)

/** How much (radians) the dots are swept around the center half way through the morph. */
private const val SCATTER_TWIST = 0.7f

/** Time drawn when nothing animates: the droid, eyes open. */
private const val STILL_TIME_SECONDS = 2f

// ---- Blink and float ----
private const val BLINK_PERIOD_SECONDS = 3.6f
private const val BLINK_SECONDS = 0.24f
private const val FLOAT_PERIOD_SECONDS = 3.6f
private const val FLOAT_AMOUNT = 0.045f

// ---- Droid geometry, in logo units (radius 1, y up) ----
private const val HEAD_CY = -0.3f
private const val HEAD_R = 0.7f
private const val EYE_X = 0.42f * HEAD_R
private const val EYE_Y = HEAD_CY + 0.45f * HEAD_R
private const val EYE_RADIUS = 0.14f * HEAD_R

// ---- Drawing ----
private const val GLOW_SPRITE_PX = 32
private const val SPRITE_BASE_DP = 3f
private const val SPRITE_DEPTH_DP = 4.4f
private const val SPRITE_SCALE = 0.85f
private const val FRONT_DEPTH_THRESHOLD = 0.92f

/** How many steps the head's gradient (bottom accent → top accent) is split into. */
private const val GRADIENT_STEPS = 12

private const val RANDOM_SEED = 7

private val GOLDEN_ANGLE = (PI * (3.0 - sqrt(5.0))).toFloat()

// Indexes inside the scratch FloatArray(4) written by the shape functions.
private const val X = 0
private const val Y = 1
private const val DEPTH = 2
private const val VISIBILITY = 3

/**
 * The About screen's mascot made of glowing dots, in the style of the particle logo:
 * the Android head (with antennae that sway and eyes that blink) floats over a pulsing
 * glow, then scatters into a swirl and gathers back into the head. When the screen opens,
 * the dots gather into the head.
 *
 * Tapping it scatters the head into the swirl, calls [onClick] while it is scattered and
 * then gathers it back.
 *
 * Only the draw phase repeats every frame; nothing recomposes while animating. If the user
 * turned animations off in the system settings, the head is drawn without motion.
 *
 * @param onClick Called once per tap, when the head has scattered (rapid taps count as one).
 * @param size Width and height of the drawing area.
 * @param staticTimeSeconds When not `null`, nothing animates and the mascot is drawn at this
 * instant (useful for previews).
 * @param staticScatter What is drawn when nothing animates: 0 = droid, 1 = swirl.
 */
@Composable
fun ParticleDroid(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    staticTimeSeconds: Float? = null,
    staticScatter: Float = GATHERED,
) {
    val context = LocalContext.current
    val animatorScale = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }
    val frozenTime = staticTimeSeconds ?: if (animatorScale == 0f) STILL_TIME_SECONDS else null

    val seeds = remember { DroidSeeds() }
    val from = remember { FloatArray(4) }
    val to = remember { FloatArray(4) }

    // Starts scattered, so the dots gather into the head when the screen opens
    val scatter = remember { Animatable(SCATTERED) }
    var taps by remember { mutableIntStateOf(0) }
    val currentOnClick by rememberUpdatedState(onClick)

    var timeSeconds by remember { mutableFloatStateOf(0f) }
    if (frozenTime == null) {
        LaunchedEffect(Unit) {
            val startNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { timeSeconds = (it - startNanos) / 1_000_000_000f }
            }
        }
        // Each tap restarts this from wherever the dots are at that moment
        LaunchedEffect(taps) {
            if (taps > 0) {
                var clicked = false
                scatter.animateTo(SCATTERED, MORPH_SPEC) {
                    if (!clicked && value >= 0.5f) {
                        clicked = true
                        currentOnClick()
                    }
                }
                if (!clicked) currentOnClick()
                delay(TAP_SWIRL_HOLD_MILLIS)
            }
            while (true) {
                scatter.animateTo(GATHERED, MORPH_SPEC)
                delay(DROID_HOLD_MILLIS)
                scatter.animateTo(SCATTERED, MORPH_SPEC)
                delay(SWIRL_HOLD_MILLIS)
            }
        }
    }

    val c = LocalAppColors.current
    val sprites = remember(c.bottomAccent, c.topAccent, c.textPrimary) {
        DroidSprites(
            gradient = Array(GRADIENT_STEPS) {
                makeGlowSprite(lerp(c.bottomAccent, c.topAccent, it / (GRADIENT_STEPS - 1f)))
            },
            highlight = makeGlowSprite(c.textPrimary),
        )
    }
    // Additive glow only looks good on dark backgrounds
    val additive = !c.isLight

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = "Mascote do DroidMentor. Toca para uma dica." }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button
            ) {
                if (frozenTime == null) taps++ else onClick()
            }
    ) {
        // timeSeconds and scatter are read ONLY here, so each frame only repeats the draw phase.
        drawDroid(
            elapsed = frozenTime ?: timeSeconds,
            scatter = if (frozenTime == null) scatter.value else staticScatter,
            seeds = seeds,
            from = from,
            to = to,
            sprites = sprites,
            glowColor = c.accent,
            shadowColor = c.textPrimary,
            additive = additive,
        )
    }
}

/** One sprite per step of the head's gradient, plus one for the dots in front. */
private class DroidSprites(val gradient: Array<ImageBitmap>, val highlight: ImageBitmap)

/** Soft round dot of [color], drawn once into a bitmap so each frame only copies it. */
private fun makeGlowSprite(color: Color, sizePx: Int = GLOW_SPRITE_PX): ImageBitmap {
    val bitmap = ImageBitmap(sizePx, sizePx)
    val center = Offset(sizePx / 2f, sizePx / 2f)
    val paint = Paint()
    paint.shader = RadialGradientShader(
        center = center,
        radius = sizePx / 2f,
        colors = listOf(color, color.copy(alpha = 0.55f), Color.Transparent),
        colorStops = listOf(0f, 0.35f, 1f),
    )
    BitmapCanvas(bitmap).drawCircle(center, sizePx / 2f, paint)
    return bitmap
}

/**
 * Fixed positions and random values, created once with a fixed seed, so the mascot looks
 * the same every time and nothing is allocated per frame.
 */
private class DroidSeeds {
    // Head dots, spread evenly over a half disk (sunflower pattern), from the center outwards
    val headX = FloatArray(HEAD_DOTS)
    val headY = FloatArray(HEAD_DOTS)

    val phase = FloatArray(PARTICLE_COUNT)
    val swirlRadius = FloatArray(PARTICLE_COUNT)
    val swirlJitter = FloatArray(PARTICLE_COUNT)

    init {
        // Sunflower over a full disk, keeping only its top half. The total grows until
        // the top half has enough dots, so the rim isn't left out.
        var total = HEAD_DOTS * 2
        var count: Int
        do {
            count = 0
            for (j in 0 until total) {
                if (count == HEAD_DOTS) break
                val r = sqrt((j + 0.5f) / total)
                val a = j * GOLDEN_ANGLE
                val y = sin(a) * r
                if (y < 0f) continue
                headX[count] = cos(a) * r * HEAD_R
                headY[count] = HEAD_CY + y * HEAD_R
                count++
            }
            total += 8
        } while (count < HEAD_DOTS)

        val random = Random(RANDOM_SEED)
        for (i in 0 until PARTICLE_COUNT) {
            phase[i] = random.nextFloat() * 2f * PI.toFloat()
            swirlRadius[i] = 0.12f + random.nextFloat().pow(0.7f) * 0.83f
            swirlJitter[i] = (random.nextFloat() - 0.5f) * 0.5f
        }
    }
}

/**
 * Draws the glow, the shadow and every dot [elapsed] seconds after the mascot appeared,
 * with the dots [scatter] of the way from the droid (0) to the swirl (1).
 */
private fun DrawScope.drawDroid(
    elapsed: Float,
    scatter: Float,
    seeds: DroidSeeds,
    from: FloatArray,
    to: FloatArray,
    sprites: DroidSprites,
    glowColor: Color,
    shadowColor: Color,
    additive: Boolean,
) {
    val logoRadius = size.minDimension / 2f
    // 1 while it looks like the droid, 0 while it is the swirl (fades the shadow)
    val droidAmount = 1f - easeInOutCubic(scatter.coerceIn(0f, 1f))

    val floatOffset = sin(elapsed * 2f * PI.toFloat() / FLOAT_PERIOD_SECONDS) * FLOAT_AMOUNT
    val pulse = 0.85f + 0.15f * sin(elapsed * 2f * PI.toFloat() / 4.8f)
    val eyeOpen = eyeOpenness(elapsed)

    // Glow behind
    drawCircle(
        Brush.radialGradient(
            listOf(glowColor.copy(alpha = 0.3f * pulse), Color.Transparent),
            center = center,
            radius = logoRadius
        )
    )
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

/**
 * Android head: the first [HEAD_DOTS] dots fill a half disk with two eye holes, the rest
 * draw the two antennae. The head is shaded like a dome, with a light sweeping across it.
 */
private fun droidPoint(i: Int, t: Float, eyeOpen: Float, seeds: DroidSeeds, out: FloatArray) {
    if (i < HEAD_DOTS) {
        var x = seeds.headX[i] + sin(t * 1.3f + seeds.phase[i]) * 0.006f
        var y = seeds.headY[i] + cos(t * 1.1f + seeds.phase[i]) * 0.006f

        // Eyes: dots inside an eye are pushed to its edge, so they outline it.
        // Closing the eye squashes the hole into a line.
        val eyeRadiusY = EYE_RADIUS * eyeOpen
        for (side in -1..1 step 2) {
            val eyeX = side * EYE_X
            val dx = (x - eyeX) / EYE_RADIUS
            val dy = (y - EYE_Y) / eyeRadiusY
            val d2 = dx * dx + dy * dy
            if (d2 < 1f) {
                if (d2 < 1e-6f) {
                    y = EYE_Y + eyeRadiusY
                } else {
                    val toEdge = 1f / sqrt(d2)
                    x = eyeX + dx * toEdge * EYE_RADIUS
                    y = EYE_Y + dy * toEdge * eyeRadiusY
                }
            }
        }

        val nx = x / HEAD_R
        val ny = (y - HEAD_CY) / HEAD_R
        val dome = sqrt((1f - nx * nx - ny * ny).coerceAtLeast(0f))
        val sweepX = sin(t * 0.8f) * 1.3f
        val sweep = exp(-(nx - sweepX) * (nx - sweepX) / 0.05f)

        out[X] = x
        out[Y] = y
        out[DEPTH] = (0.2f + 0.6f * dome + 0.35f * sweep).coerceIn(0f, 1f)
        out[VISIBILITY] = 1f
    } else {
        val k = i - HEAD_DOTS
        val side = if (k < ANTENNA_DOTS) -1f else 1f
        val f = (k % ANTENNA_DOTS) / (ANTENNA_DOTS - 1f)
        // Left one at 120°, right one at 60°; the tip sways more than the base
        val angle = (PI / 2).toFloat() - side * (PI / 6).toFloat() +
            sin(t * 2.2f + side) * 0.08f * f
        val r = HEAD_R * (0.85f + 0.45f * f)
        out[X] = cos(angle) * r
        out[Y] = HEAD_CY + sin(angle) * r
        out[DEPTH] = if (k % ANTENNA_DOTS == ANTENNA_DOTS - 1) 1f else 0.7f
        out[VISIBILITY] = 1f
    }
}

/** Swirl: three spiral arms rotating around the center, brighter in the core. */
private fun swirlPoint(i: Int, t: Float, seeds: DroidSeeds, out: FloatArray) {
    val r = seeds.swirlRadius[i]
    val arm = (i % 3) * (2f * PI.toFloat() / 3f)
    val angle = arm + r * 3.6f + t * 0.7f + seeds.swirlJitter[i]
    out[X] = cos(angle) * r
    out[Y] = sin(angle) * r * 0.8f
    out[DEPTH] = (0.25f + 0.65f * (1f - r) + 0.15f * sin(t * 2f + seeds.phase[i])).coerceIn(0f, 1f)
    out[VISIBILITY] = 1f
}

/** 1 = eyes open. Once per [BLINK_PERIOD_SECONDS] they close and open quickly. */
private fun eyeOpenness(t: Float): Float {
    val phase = t % BLINK_PERIOD_SECONDS
    val blinkStart = BLINK_PERIOD_SECONDS - BLINK_SECONDS
    if (phase < blinkStart) return 1f
    val x = (phase - blinkStart) / BLINK_SECONDS
    return 1f - 0.9f * sin(PI.toFloat() * x)
}

/** Slow at the start and at the end, fast in the middle. */
private fun easeInOutCubic(x: Float): Float =
    if (x < 0.5f) 4f * x * x * x
    else {
        val inverse = -2f * x + 2f
        1f - inverse * inverse * inverse / 2f
    }

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

//==================================== PREVIEWS ====================================//

@Preview
@Composable
private fun ParticleDroidDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ParticleDroid(onClick = {}, staticTimeSeconds = STILL_TIME_SECONDS)
        }
    }
}

@Preview
@Composable
private fun ParticleDroidLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ParticleDroid(onClick = {}, staticTimeSeconds = STILL_TIME_SECONDS)
        }
    }
}

@Preview
@Composable
private fun ParticleDroidSwirlPreview() {
    DroidMentorTheme(colors = PresetThemes[1]) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ParticleDroid(onClick = {}, staticTimeSeconds = 10.6f, staticScatter = SCATTERED)
        }
    }
}

// Animated: check it in Interactive Mode (tap it to see it scatter and gather back).
@Preview
@Composable
private fun ParticleDroidAnimatedPreview() {
    DroidMentorTheme(colors = PresetThemes[2]) {
        Box(Modifier.background(LocalAppColors.current.background)) {
            ParticleDroid(onClick = {})
        }
    }
}
