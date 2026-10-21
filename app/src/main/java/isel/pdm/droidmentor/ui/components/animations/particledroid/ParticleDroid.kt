package isel.pdm.droidmentor.ui.components.animations.particledroid

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import kotlinx.coroutines.delay

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

/** Time drawn when nothing animates: the droid, eyes open. */
private const val STILL_TIME_SECONDS = 2f

/**
 * The About screen's mascot made of glowing dots, in the style of the particle logo:
 * the Android head (with antennae that sway and eyes that blink) floats, then scatters
 * into a swirl and gathers back into the head. When the screen opens,
 * the dots gather into the head.
 *
 * Tapping it scatters the head into the swirl, calls [onClick] while it is scattered and
 * then gathers it back.
 *
 * Only the draw phase repeats every frame; nothing recomposes while animating. If the user
 * turned animations off in the system settings, the head is drawn without motion.
 *
 * The rest of the mascot is split by topic in this folder:
 * - `ParticleDroidDrawing.kt`: how the shadow and each dot are drawn.
 * - `ParticleDroidShapes.kt`: where each dot is in the droid and in the swirl.
 * - `ParticleDroidSeeds.kt`: the fixed positions and random values of the dots.
 * - `ParticleDroidSprites.kt`: the glow bitmaps the dots are drawn with.
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
    size: Dp = 250.dp,
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
    val sprites = rememberDroidSprites()
    // Additive glow only looks good on dark backgrounds
    val additive = !c.isLight

    val description = stringResource(R.string.about_mascot_description)

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = description }
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
            shadowColor = c.textPrimary,
            additive = additive,
        )
    }
}

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