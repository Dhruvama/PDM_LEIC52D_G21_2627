package isel.pdm.droidmentor.ui.components.animations.particlelogo

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.readableOn

const val PARTICLE_LOGO_TAG = "particle_logo"

// The Title screen's mascot is drawn in a 300×330 dp box, so its swirl has a radius of
// min(300, 330) / 2 = 150 dp. The entry swirl has that same size on screen.
private const val TITLE_SWIRL_RADIUS_DP = 150f

/** Time drawn when the user turned animations off: the middle of the sphere's hold. */
private const val REDUCED_MOTION_TIME_SECONDS = 7f

/** Colors of the particle logo. Chosen by the user in Settings (built by a teammate); null = use the theme. */
data class ParticleLogoColors(
    val main: Color,       // most dots
    val second: Color,     // every 3rd dot
    val highlight: Color,  // dots in front (highest depth)
)

/**
 * Decorative logo made of a cloud of small dots that loops through three shapes,
 * morphing smoothly between them: a drifting swarm, a rotating sphere and a rotating
 * dotted double helix. Dots "in front" are bigger and more opaque.
 *
 * Only the draw phase repeats every frame; the composable itself does not recompose
 * while animating. If the user turned animations off in the system settings, the
 * sphere is drawn without motion.
 *
 * The rest of the logo is split by topic in this folder:
 * - `ParticleLogoDrawing.kt`: the loop timeline and how each dot is drawn.
 * - `ParticleLogoShapes.kt`: where each dot is in every shape.
 * - `ParticleLogoSeeds.kt`: the random values of the swarm and the entry swirl.
 * - `ParticleLogoSprites.kt`: the glow bitmaps the dots are drawn with.
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
fun ParticleLogoComponent(
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

    val sprites = rememberGlowSprites(logoColors)

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

/**
 * The glow sprites in the logo's colors: the user's [logoColors], or the theme's accents when `null`.
 * Created once (and again only if a color changes), never per frame.
 * Holds no UI, so it has no preview.
 */
@Composable
private fun rememberGlowSprites(logoColors: ParticleLogoColors?): GlowSprites {
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

    return remember(mainColor, secondColor, highlightColor) {
        GlowSprites(
            main = makeGlowSprite(mainColor),
            second = makeGlowSprite(secondColor),
            highlight = makeGlowSprite(highlightColor),
        )
    }
}

/**
 * Clock of the entry swirl, from a frame time of `withFrameNanos`. The Title screen turns its
 * swirl with this same clock, so both swirls line up while the screens cross-fade. It wraps
 * around after 1000 s so the float value stays precise.
 */
private fun swirlClockSeconds(frameNanos: Long): Float =
    ((frameNanos / 1_000_000L) % 1_000_000L) / 1000f

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun ParticleLogoComponentSwarmPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogoComponent(staticTimeSeconds = 1f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoComponentSpherePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogoComponent(staticTimeSeconds = 7f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoComponentHelixPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogoComponent(staticTimeSeconds = 12f)
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticleLogoComponentAnimatedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ParticleLogoComponent()
    }
}

// Animated: check it in Interactive Mode (restart it to replay the entry).
@Preview(showBackground = true)
@Composable
private fun ParticleLogoComponentAssembleOnStartPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(
            modifier = Modifier.size(width = 360.dp, height = 640.dp),
            contentAlignment = Alignment.Center,
        ) {
            ParticleLogoComponent(
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
private fun ParticleLogoComponentGlowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.inverseSurface)) {
            ParticleLogoComponent(glow = true, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoComponentSphereDarkGlowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogoComponent(glow = true, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoComponentSphereLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogoComponent(glow = false, staticTimeSeconds = 7f)
        }
    }
}

@Preview
@Composable
private fun ParticleLogoComponentCustomColorsPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogoComponent(
                glow = true,
                logoColors = previewCustomColors,
                staticTimeSeconds = 7f,
            )
        }
    }
}

@Preview
@Composable
private fun ParticleLogoComponentUnreadableColorFixedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(modifier = Modifier.background(LocalAppColors.current.background)) {
            ParticleLogoComponent(
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
