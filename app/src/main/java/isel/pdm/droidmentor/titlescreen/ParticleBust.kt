package isel.pdm.droidmentor.titlescreen

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Test tag of [ParticleBust]. */
const val PARTICLE_BUST_TAG = "particle_bust"

// ---------------------------------------------------------------------------
// Shape constants
// ---------------------------------------------------------------------------

private const val HEAD_COUNT = 380
private const val NECK_COUNT = 60
private const val TORSO_COUNT = 560
private const val DOT_COUNT = HEAD_COUNT + NECK_COUNT + TORSO_COUNT // 1000
private const val NECK_RING_SIZE = 12

private const val SPIN_SPEED = 0.45f
private const val GATHER_DURATION_MS = 1800
private const val REDUCED_MOTION_TIME_SECONDS = 2f

private val GOLDEN = (PI * (3.0 - sqrt(5.0))).toFloat()
private const val TWO_PI = (2.0 * PI).toFloat()

/** Plus blending (additive), allocated once. Used only when glow is on. */
private val AdditiveBlend: Xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)

// ---------------------------------------------------------------------------
// Public composable
// ---------------------------------------------------------------------------

/**
 * A person's bust drawn with 1000 dots (head, neck and torso) that slowly spins
 * around the vertical axis.
 *
 * When [gathered] is false the dots float in a loose cloud; when it becomes true
 * they fly into the bust shape (head first, torso last). On first composition the
 * dots start scattered and gather, so the bust "assembles" when the screen opens.
 *
 * Purely decorative: it has no content description.
 *
 * @param gathered true = dots form the bust; false = dots scattered in a cloud.
 * @param modifier modifier applied to the canvas.
 * @param glow additive blending with a halo around each dot; only looks good on dark backgrounds.
 * @param staticTimeSeconds when not null there is no animation and this instant is drawn
 * (previews / reduced motion).
 */
@Composable
fun ParticleBust(
    gathered: Boolean,
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    staticTimeSeconds: Float? = null,
) {
    val context = LocalContext.current

    // Reduced motion: system animations turned off -> gathered and static.
    val reducedMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    val effectiveStaticTime: Float? =
        staticTimeSeconds ?: if (reducedMotion) REDUCED_MOTION_TIME_SECONDS else null
    val isStatic = effectiveStaticTime != null
    val effectiveGathered = gathered || reducedMotion

    // Starts false so the first frame is scattered; the effect flips it to trigger the gathering.
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val animatedProgress = animateFloatAsState(
        targetValue = if (effectiveGathered && started) 1f else 0f,
        animationSpec = tween(GATHER_DURATION_MS),
        label = "bustFormProgress",
    )
    // Static mode skips the animation so previews show the final state immediately.
    val staticProgress = if (effectiveGathered) 1f else 0f

    // Time source. Read ONLY inside the draw lambda below: only the draw phase
    // repeats every frame; composition and layout are not re-run.
    val timeSeconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isStatic) {
        if (isStatic) return@LaunchedEffect
        var startNanos = -1L
        while (true) {
            withFrameNanos { frameNanos ->
                if (startNanos < 0L) startNanos = frameNanos
                timeSeconds.floatValue = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    // Allocated once: seeds, scratch arrays and paint.
    val seeds = remember { CloudSeeds.create(DOT_COUNT) }
    val cloudScratch = remember { FloatArray(3) }
    val bustScratch = remember { FloatArray(3) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }

    // Theme colors read once, outside the draw loop.
    val colors = MaterialTheme.colorScheme
    val onSurfaceArgb = colors.onSurface.toArgb()
    val tertiaryArgb = colors.tertiary.toArgb()
    val primaryArgb = colors.primary.toArgb()

    Canvas(modifier = modifier.testTag(PARTICLE_BUST_TAG)) {
        // State reads happen here, in the draw phase only.
        val t = effectiveStaticTime ?: timeSeconds.floatValue
        val progress = if (isStatic) staticProgress else animatedProgress.value
        val spin = t * SPIN_SPEED

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val scale = min(size.width, size.height) * 0.52f

        paint.xfermode = if (glow) AdditiveBlend else null

        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            for (i in 0 until DOT_COUNT) {
                val m = easeInOutCubic(
                    (progress * 1.5f - (i / DOT_COUNT.toFloat()) * 0.5f).coerceIn(0f, 1f)
                )
                fillCloud(i, t, seeds, cloudScratch)
                fillBust(i, spin, bustScratch)

                val x = lerp(cloudScratch[0], bustScratch[0], m)
                val y = lerp(cloudScratch[1], bustScratch[1], m)
                val depth = lerp(cloudScratch[2], bustScratch[2], m)

                var alpha = 0.18f + depth * 0.72f
                if (y > 0.45f) alpha *= max(0f, 1f - (y - 0.45f) / 0.25f)
                if (alpha < 0.01f) continue

                val radius = (1.3f + depth * 2.3f) * density
                val argb = when {
                    depth > 0.88f -> onSurfaceArgb
                    i % 3 == 0 -> tertiaryArgb
                    else -> primaryArgb
                }
                val px = centerX + x * scale
                val py = centerY + y * scale

                paint.color = argb
                if (glow) {
                    paint.alpha = (alpha * 0.25f * 255f).toInt().coerceIn(0, 255)
                    native.drawCircle(px, py, radius * 2.5f, paint)
                }
                paint.alpha = (alpha * 255f).toInt().coerceIn(0, 255)
                native.drawCircle(px, py, radius, paint)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shape functions: each fills out[0] = x, out[1] = y, out[2] = depth
// ---------------------------------------------------------------------------

/** Routes dot [i] (0..999) to the head, neck or torso function. */
private fun fillBust(i: Int, spin: Float, out: FloatArray) {
    when {
        i < HEAD_COUNT -> fillHead(i, spin, out)
        i < HEAD_COUNT + NECK_COUNT -> fillNeck(i - HEAD_COUNT, spin, out)
        else -> fillTorso(i - HEAD_COUNT - NECK_COUNT, spin, out)
    }
}

/** Head: Fibonacci sphere, k in 0..379. */
private fun fillHead(k: Int, spin: Float, out: FloatArray) {
    val yy = 1f - k / (HEAD_COUNT - 1f) * 2f
    val r = sqrt(1f - yy * yy)
    val a = k * GOLDEN + spin
    out[0] = cos(a) * r * 0.24f
    out[1] = -0.42f + yy * 0.29f
    out[2] = (sin(a) * r + 1f) / 2f
}

/** Neck: 5 rings of 12 dots, k in 0..59. */
private fun fillNeck(k: Int, spin: Float, out: FloatArray) {
    val row = k / NECK_RING_SIZE
    val col = k % NECK_RING_SIZE
    val a = col / NECK_RING_SIZE.toFloat() * TWO_PI + spin + row * 0.25f
    out[0] = cos(a) * 0.085f
    out[1] = -0.12f + row * 0.03f
    out[2] = (sin(a) + 1f) / 2f
}

/** Torso: spiral that widens into the shoulders, k in 0..559. */
private fun fillTorso(k: Int, spin: Float, out: FloatArray) {
    val v = k / (TORSO_COUNT - 1f)
    val s = min(1f, v * 2.4f)
    val width = 0.11f + 0.55f * sqrt(1f - (1f - s) * (1f - s))
    val a = k * GOLDEN + spin
    out[0] = cos(a) * width
    out[1] = 0.03f + v * 0.62f
    out[2] = (sin(a) + 1f) / 2f
}

/** Scattered position of dot [i] at time [t], from its fixed random seeds. */
private fun fillCloud(i: Int, t: Float, seeds: CloudSeeds, out: FloatArray) {
    val wobble = t * seeds.speed[i] + seeds.phase[i]
    out[0] = cos(seeds.angle[i]) * seeds.radius[i] + sin(wobble) * 0.06f
    out[1] = sin(seeds.angle[i]) * seeds.radius[i] * 0.9f + cos(wobble) * 0.06f
    out[2] = 0.4f + 0.3f * sin(t + seeds.phase[i])
}

/** Per-dot random seeds for the cloud, created once with a fixed seed. */
private class CloudSeeds(
    val angle: FloatArray,
    val radius: FloatArray,
    val phase: FloatArray,
    val speed: FloatArray,
) {
    companion object {
        fun create(count: Int): CloudSeeds {
            val random = Random(seed = 42)
            return CloudSeeds(
                angle = FloatArray(count) { random.nextFloat() * TWO_PI },
                radius = FloatArray(count) { 0.6f + random.nextFloat() * 0.9f },   // 0.6..1.5
                phase = FloatArray(count) { random.nextFloat() * TWO_PI },
                speed = FloatArray(count) { 0.5f + random.nextFloat() },           // 0.5..1.5
            )
        }
    }
}

private fun easeInOutCubic(x: Float): Float =
    if (x < 0.5f) {
        4f * x * x * x
    } else {
        val u = -2f * x + 2f
        1f - u * u * u / 2f
    }

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private const val PREVIEW_STATIC_TIME = 2f
private val PreviewWidth = 300.dp
private val PreviewHeight = 330.dp

@Preview(showBackground = true)
@Composable
private fun ParticleBustGatheredPreview() {
    DroidMentorTheme(dynamicColor = false) {
        ParticleBust(
            gathered = true,
            staticTimeSeconds = PREVIEW_STATIC_TIME,
            modifier = Modifier.size(PreviewWidth, PreviewHeight),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleBustScatteredPreview() {
    DroidMentorTheme(dynamicColor = false) {
        ParticleBust(
            gathered = false,
            staticTimeSeconds = PREVIEW_STATIC_TIME,
            modifier = Modifier.size(PreviewWidth, PreviewHeight),
        )
    }
}

/** Run in Interactive Mode to check the rotation and the assembly. */
@Preview(showBackground = true)
@Composable
private fun ParticleBustAnimatedPreview() {
    DroidMentorTheme(dynamicColor = false) {
        ParticleBust(
            gathered = true,
            modifier = Modifier.size(PreviewWidth, PreviewHeight),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleBustGlowPreview() {
    DroidMentorTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.inverseSurface)
                .size(PreviewWidth, PreviewHeight),
        ) {
            ParticleBust(
                gathered = true,
                glow = true,
                staticTimeSeconds = PREVIEW_STATIC_TIME,
                modifier = Modifier.size(PreviewWidth, PreviewHeight),
            )
        }
    }
}