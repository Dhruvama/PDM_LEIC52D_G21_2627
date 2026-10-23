package isel.pdm.droidmentor.ui.components.animations.particlelogo

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Where each dot of the particle logo is in every shape.

internal const val SHAPE_SWARM = 0
internal const val SHAPE_SPHERE = 1
internal const val SHAPE_HELIX = 2
internal const val SHAPE_ENTRY_CLOUD = 3

// Indexes inside the scratch FloatArray(4) written by the shape functions.
internal const val X = 0
internal const val Y = 1
internal const val DEPTH = 2
internal const val VISIBILITY = 3

private const val HELIX_LEVELS = 30
private const val HELIX_SLOTS_PER_LEVEL = 6
private const val HELIX_SLOTS = HELIX_LEVELS * HELIX_SLOTS_PER_LEVEL

private val GOLDEN_ANGLE = (PI * (3.0 - sqrt(5.0))).toFloat()

/**
 * Writes into [out] where particle [i] is in the given [shape] at time [t].
 */
internal fun shapePoint(
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

/** Value between [start] (at fraction 0) and [stop] (at fraction 1). */
internal fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
