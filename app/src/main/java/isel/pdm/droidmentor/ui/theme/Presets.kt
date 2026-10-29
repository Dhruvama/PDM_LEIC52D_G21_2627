package isel.pdm.droidmentor.ui.theme

import androidx.compose.ui.graphics.Color

/** A pair of glows with its background for each mode. */
private data class PresetSpec(
    val top: Color,
    val bottom: Color,
    val darkBackground: Color,
    val lightBackground: Color,
) {
    fun toAppColors(light: Boolean) = AppColors(
        top = ColorPair(top),
        bottom = ColorPair(bottom),
        background = if (light) lightBackground else darkBackground,
    )
}

private val presetSpecs = listOf(
    PresetSpec(Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF040504), Color(0xFFF5F7F2)),
    PresetSpec(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF05040A), Color(0xFFF6F5FB)),
    PresetSpec(Color(0xFFF97316), Color(0xFFEC4899), Color(0xFF0A0505), Color(0xFFFBF6F4)),
)

val DefaultDarkColors = presetSpecs.first().toAppColors(light = false)
val DefaultLightColors = presetSpecs.first().toAppColors(light = true)

/** All the ready-made themes: first the dark ones, then the light ones. */
val PresetThemes: List<AppColors> =
    presetSpecs.map { it.toAppColors(light = false) } +
            presetSpecs.map { it.toAppColors(light = true) }

