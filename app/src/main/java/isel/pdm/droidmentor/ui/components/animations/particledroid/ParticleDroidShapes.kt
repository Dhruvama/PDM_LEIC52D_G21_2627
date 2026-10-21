package isel.pdm.droidmentor.ui.components.animations.particledroid

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

// Where each dot of the mascot is in the droid and in the swirl.

// Indexes inside the scratch FloatArray(4) written by the shape functions.
internal const val X = 0
internal const val Y = 1
internal const val DEPTH = 2
internal const val VISIBILITY = 3

// ---- Droid geometry, in logo units (radius 1, y up) ----
internal const val HEAD_CY = -0.3f
internal const val HEAD_R = 0.7f
private const val EYE_X = 0.42f * HEAD_R
private const val EYE_Y = HEAD_CY + 0.45f * HEAD_R
private const val EYE_RADIUS = 0.14f * HEAD_R

// ---- Blink ----
private const val BLINK_PERIOD_SECONDS = 3.6f
private const val BLINK_SECONDS = 0.24f

/**
 * Android head: the first [HEAD_DOTS] dots fill a half disk with two eye holes, the rest
 * draw the two antennae. The head is shaded like a dome, with a light sweeping across it.
 */
internal fun droidPoint(i: Int, t: Float, eyeOpen: Float, seeds: DroidSeeds, out: FloatArray) {
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
internal fun swirlPoint(i: Int, t: Float, seeds: DroidSeeds, out: FloatArray) {
    val r = seeds.swirlRadius[i]
    val arm = (i % 3) * (2f * PI.toFloat() / 3f)
    val angle = arm + r * 3.6f + t * 0.7f + seeds.swirlJitter[i]
    out[X] = cos(angle) * r
    out[Y] = sin(angle) * r * 0.8f
    out[DEPTH] = (0.25f + 0.65f * (1f - r) + 0.15f * sin(t * 2f + seeds.phase[i])).coerceIn(0f, 1f)
    out[VISIBILITY] = 1f
}

/** 1 = eyes open. Once per [BLINK_PERIOD_SECONDS] they close and open quickly. */
internal fun eyeOpenness(t: Float): Float {
    val phase = t % BLINK_PERIOD_SECONDS
    val blinkStart = BLINK_PERIOD_SECONDS - BLINK_SECONDS
    if (phase < blinkStart) return 1f
    val x = (phase - blinkStart) / BLINK_SECONDS
    return 1f - 0.9f * sin(PI.toFloat() * x)
}