package isel.pdm.droidmentor.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Invariants every [AppColors] must keep, whatever colors it is built from:
 * the presets, and also arbitrary glows (like the wallpaper's dynamic colors).
 */
class AppColorsTest {

    // ----------------------------------------- Presets -------------------------------------------------

    @Test
    fun `every preset is readable`() {
        PresetThemes.forEachIndexed { i, c -> assertReadable(c, "preset $i") }
    }

    // --------------------- Arbitrary colors to ensure readability is accomplished ----------------------

    @Test
    fun `theme built from any color is readable`() {
        for (light in listOf(true, false))
            for (h in 0 until 360 step 15)
                for (s in listOf(0.3f, 0.7f, 1f))
                    for (v in listOf(0.4f, 0.7f, 1f)) {
                        val glow = Color.hsv(h.toFloat(), s, v)
                        val c = AppColors(
                            top = GlowSpec(glow),
                            bottom = GlowSpec(Color.hsv((h + 120f) % 360f, s, v)),
                            background = glow.toBackground(light),
                        )
                        val label = "light=$light h=$h s=$s v=$v"
                        assertEquals(label, light, c.isLight)
                        assertReadable(c, label)
                    }
    }

    @Test
    fun `rawAccent is used instead of the glow when given`() {
        val bg = Color(0xFF040504)
        val c = AppColors(
            top = GlowSpec(glow = Color.Red, rawAccent = Color.Cyan),
            bottom = GlowSpec(Color.Blue),
            background = bg,
        )
        assertEquals(Color.Cyan.readableOn(bg), c.topAccent)
    }

    // Helpers

    private fun assertReadable(c: AppColors, label: String) {
        fun check(name: String, fg: Color, bg: Color, min: Float) {
            val ratio = contrastRatio(fg, bg)
            assertTrue("$label: $name contrast is $ratio, expected >= $min", ratio >= min)
        }
        check("topAccent / background", c.topAccent, c.background, 3f)
        check("bottomAccent / background", c.bottomAccent, c.background, 3f)
        check("onAccent / accent", c.onAccent, c.accent, 4.5f)
        check("textPrimary / background", c.textPrimary, c.background, 7f)
        check("textPrimary / surface", c.textPrimary, c.surface, 7f)
        check("onCrossGradient / bottomAccent", c.onCrossGradient, c.bottomAccent, 3f)
        check("onCrossGradient / topAccent", c.onCrossGradient, c.topAccent, 3f)
    }
}