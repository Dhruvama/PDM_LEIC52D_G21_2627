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
        top = GlowSpec(top),
        bottom = GlowSpec(bottom),
        background = if (light) lightBackground else darkBackground,
    )
}

private val presetSpecs = listOf(
    // Default (lime / green)
    PresetSpec(Color(0xFFC3E84A), Color(0xFF3DDC4B), Color(0xFF040504), Color(0xFFF5F7F2)),
    // Violet / blue
    PresetSpec(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF05040A), Color(0xFFF6F5FB)),
    // Orange / pink
    PresetSpec(Color(0xFFF97316), Color(0xFFEC4899), Color(0xFF0A0505), Color(0xFFFBF6F4)),
)

val DefaultDarkColors = presetSpecs.first().toAppColors(light = false)
val DefaultLightColors = presetSpecs.first().toAppColors(light = true)

/** All the ready-made themes: first the dark ones, then the light ones. */
val PresetThemes: List<AppColors> =
    presetSpecs.map { it.toAppColors(light = false) } +
            presetSpecs.map { it.toAppColors(light = true) }

/**
 * Allowed background colors for each mode. The gap between them avoids
 * mid-tone backgrounds where neither dark nor light text is readable.
 */
object BackgroundLimits {
    fun saturation(light: Boolean) = if (light) 0f..0.2f else 0f..0.5f
    fun value(light: Boolean) = if (light) 0.9f..1f else 0f..0.15f

    /** Brightness used when turning a color into a background (inside [value]). */
    fun defaultValue(light: Boolean) = if (light) 0.97f else 0.04f
}