package isel.pdm.droidmentor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.pdm.droidmentor.R
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.ui.theme.toHsv

/**
 * Color picker with three sliders: hue, saturation and brightness (HSV).
 *
 * @param color The current color (kept by the caller).
 * @param onColorChange Called while the user drags (use it for a live preview).
 * @param onColorChangeFinished Called when the user lifts the finger (use it to save).
 * @param saturationRange Allowed saturation (0..1).
 * @param valueRange Allowed brightness (0..1).
 */
@Composable
fun HsvColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    onColorChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    saturationRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    // HSV kept as state, initialized from the color. It's only re-synced from `color`
    // when the color changes from outside (e.g. a preset), so the hue isn't lost
    // when the color turns grey or black.
    val initial = remember { color.toHsv() }
    var hue by remember { mutableFloatStateOf(initial[0]) }
    var sat by remember { mutableFloatStateOf(initial[1]) }
    var value by remember { mutableFloatStateOf(initial[2]) }

    if (Color.hsv(hue, sat, value).toArgb() != color.toArgb()) {
        val (h, s, v) = color.toHsv()
        hue = h
        sat = s
        value = v
    }

    // Updates the state (keeping saturation and brightness inside the allowed ranges)
    // and reports the new color.
    fun emit(h: Float = hue, s: Float = sat, v: Float = value) {
        hue = h
        sat = s.coerceIn(saturationRange)
        value = v.coerceIn(valueRange)
        onColorChange(Color.hsv(hue, sat, value))
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LabeledSlider(
            label = stringResource(R.string.color_picker_hue),
            value = hue,
            range = 0f..360f,
            gradient = (0..360 step 60).map { Color.hsv(it.toFloat(), 1f, 1f) },
            onValueChange = { emit(h = it) },
            onValueChangeFinished = onColorChangeFinished,
        )
        LabeledSlider(
            label = stringResource(R.string.color_picker_saturation),
            value = sat,
            range = saturationRange,
            gradient = listOf(
                Color.hsv(hue, saturationRange.start, value),
                Color.hsv(hue, saturationRange.endInclusive, value),
            ),
            onValueChange = { emit(s = it) },
            onValueChangeFinished = onColorChangeFinished,
        )
        LabeledSlider(
            label = stringResource(R.string.color_picker_brightness),
            value = value,
            range = valueRange,
            gradient = listOf(
                Color.hsv(hue, sat, valueRange.start),
                Color.hsv(hue, sat, valueRange.endInclusive),
            ),
            onValueChange = { emit(v = it) },
            onValueChangeFinished = onColorChangeFinished,
        )
    }
}


/** A label with a gradient slider below it. The value is clamped to [range].
 * @param label The label text.
 * @param value The current value.
 * @param range The allowed range.
 * @param gradient The gradient colors.
 * @param onValueChange Called while the user drags (use it for a live preview).
 * @param onValueChangeFinished Called when the user lifts the finger (use it to save).
 * */
@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    gradient: List<Color>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = LocalAppColors.current.textSecondary,
    )
    GradientSlider(
        value = value.coerceIn(range),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = range,
        brush = Brush.horizontalGradient(gradient),
    )
}

@Preview()
@Composable
fun HsvColorPickerPreview() {
    DroidMentorTheme {
        val c = LocalAppColors.current
        var color by remember { mutableStateOf(Color.Green) }
        Box(
            Modifier.glowBackground(c)
        ){
            HsvColorPicker(
                color = color,
                onColorChange = { color = it },
                onColorChangeFinished = { }
            )
        }
    }
}
