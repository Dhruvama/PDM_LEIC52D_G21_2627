package isel.pdm.droidmentor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.ui.theme.LocalAppColors

/**
 * A composable that displays a slider with a gradient background.
 * @param value The current value of the slider.
 * @param onValueChange The callback that is triggered when the value of the slider changes.
 * @param onValueChangeFinished The callback that is triggered when the user finishes moving the slider.
 * @param brush The brush that defines the gradient background of the slider.
 * @param modifier The modifier to be applied to the slider.
 * @param valueRange The range of values that the slider can take.
 */
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        // Gradient bar
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = LocalAppColors.current.textPrimary,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            )
        )
    }
}

@Preview
@Composable
fun GradientSliderPreview() {
    GradientSlider(
        value = 0.5f,
        onValueChange = {},
        onValueChangeFinished = {},
        brush = Brush.horizontalGradient(listOf(Color.Red, Color.Green, Color.Blue)),
        modifier = Modifier.padding(16.dp)
    )
}