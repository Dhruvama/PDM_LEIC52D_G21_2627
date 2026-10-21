package isel.pdm.droidmentor.ui.components.animations.particledroid

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ---- Particles ----
internal const val PARTICLE_COUNT = 420
internal const val ANTENNA_DOTS = 14
internal const val HEAD_DOTS = PARTICLE_COUNT - 2 * ANTENNA_DOTS

private const val RANDOM_SEED = 7

private val GOLDEN_ANGLE = (PI * (3.0 - sqrt(5.0))).toFloat()

/**
 * Fixed positions and random values, created once with a fixed seed, so the mascot looks
 * the same every time and nothing is allocated per frame.
 */
internal class DroidSeeds {
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