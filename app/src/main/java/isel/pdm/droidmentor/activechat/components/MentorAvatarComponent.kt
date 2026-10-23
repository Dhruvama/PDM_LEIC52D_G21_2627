package isel.pdm.droidmentor.activechat.components

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import kotlin.math.PI
import kotlin.math.sin

private const val RINGS = 12
private const val STATIC_TIME = 0.6f

/**
 * Round avatar of the mentor: a spiral of thin rings, shown next to the mentor's messages
 * and the thinking indicator. Decorative, so it has no content description.
 *
 * When [animated] stops, the spiral freezes where it is instead of jumping back.
 * With reduced motion (animations turned off in the system settings) it never moves.
 *
 * @param modifier Modifier applied to the avatar.
 * @param size Diameter of the avatar.
 * @param animated `true` only while the mentor is thinking or writing, to make the spiral turn.
 */
@Composable
fun MentorAvatarComponent(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    animated: Boolean = false,
) {
    val colors = LocalAppColors.current
    val time = rememberSpiralTime(animated)
    val ringStrokeWidth = max(0.8.dp, size / 40)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.border, shape = CircleShape),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // time is read only here, in the draw phase: each new frame only redraws
            // the Canvas, without recomposing the avatar.
            drawSpiral(
                time = time.floatValue,
                firstColor = colors.topAccent,
                lastColor = colors.bottomAccent,
                highlightColor = colors.textPrimary,
                strokeWidth = ringStrokeWidth.toPx(),
            )
        }
    }
}

/**
 * Seconds the spiral has turned, advancing every frame while [animated] is `true`.
 * Stays still when not animated or with reduced motion.
 *
 * Returned as a [FloatState] so it is read only where it is drawn.
 * Holds no UI, so it has no preview.
 *
 * @param animated Whether the spiral should turn.
 */
@Composable
private fun rememberSpiralTime(animated: Boolean): FloatState {
    val reducedMotion = isReducedMotion(LocalContext.current)
    val time = remember { mutableFloatStateOf(STATIC_TIME) }
    if (animated && !reducedMotion) {
        LaunchedEffect(Unit) {
            var lastFrameNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameNanos ->
                    // Continue from the current time, so a restart does not make the spiral jump.
                    time.floatValue += (frameNanos - lastFrameNanos) / 1_000_000_000f
                    lastFrameNanos = frameNanos
                }
            }
        }
    }
    return time
}

/**
 * Whether animations are turned off in the system settings.
 */
private fun isReducedMotion(context: Context): Boolean =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f

/**
 * Draws the [RINGS] rings of the spiral, centered, at the given [time].
 * Colors go from [firstColor] to [lastColor], and the rings facing forward
 * are lightened towards [highlightColor].
 */
private fun DrawScope.drawSpiral(
    time: Float,
    firstColor: Color,
    lastColor: Color,
    highlightColor: Color,
    strokeWidth: Float,
) {
    val rx = size.minDimension * 0.36f
    val ry = rx * 0.38f
    val ovalTopLeft = Offset(center.x - rx, center.y - ry)
    val ovalSize = Size(rx * 2, ry * 2)
    val stroke = Stroke(width = strokeWidth)

    for (k in 0 until RINGS) {
        val f = k / (RINGS - 1).toFloat()
        val angleRadians = k * PI.toFloat() / RINGS + time * (0.9f + k * 0.05f)
        val depth = (sin(angleRadians * 2) + 1) / 2

        var color = lerp(firstColor, lastColor, f)
        if (depth > 0.85f) {
            color = lerp(color, highlightColor, (depth - 0.85f) / 0.15f * 0.7f)
        }

        rotate(degrees = angleRadians * 180f / PI.toFloat(), pivot = center) {
            drawOval(
                color = color,
                topLeft = ovalTopLeft,
                size = ovalSize,
                alpha = 0.35f + depth * 0.55f,
                style = stroke,
            )
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun MentorAvatarComponentStaticSizesPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MentorAvatarComponent(size = 28.dp)
            MentorAvatarComponent(size = 44.dp)
            MentorAvatarComponent(size = 96.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorAvatarComponentAnimatedPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorAvatarComponent(size = 96.dp, animated = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorAvatarComponentTogglePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var on by remember { mutableStateOf(true) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MentorAvatarComponent(size = 96.dp, animated = on)
            Button(onClick = { on = !on }) {
                Text(text = if (on) "Stop" else "Start")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorAvatarComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        MentorAvatarComponent(size = 44.dp)
    }
}
