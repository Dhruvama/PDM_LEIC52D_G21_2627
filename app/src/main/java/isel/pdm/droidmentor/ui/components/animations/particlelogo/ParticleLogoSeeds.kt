package isel.pdm.droidmentor.ui.components.animations.particlelogo

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val SWARM_RANDOM_SEED = 42

/**
 * Random values for the swarm shape and for the entry cloud, created once per particle
 * count with a fixed seed, so both look the same every time and nothing is allocated per frame.
 */
internal class SwarmSeeds(particleCount: Int) {
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
