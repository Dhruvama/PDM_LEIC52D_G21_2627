package isel.pdm.droidmentor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext


/**
 * The colors the app uses when the user hasn't chosen any:
 * the wallpaper colors on Android 12+, otherwise the fixed defaults.
 * Light or dark following the system.
 */
@Composable
fun systemAppColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
): AppColors = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        scheme.toAppColors()
    }

    darkTheme -> DefaultDarkColors
    else -> DefaultLightColors
}

/**
 * App theme.
 *
 * @param darkTheme Whether to use the dark theme. Defaults to the system setting.
 * @param dynamicColor Whether to use the dynamic colors on Android 12+. Defaults to true
 * @param colors The colors chosen by the user, or null to use [systemAppColors].
 * @param content The content to display with the theme.
 *
 * Provides the [AppColors] through [LocalAppColors] and fills the Material
 * color scheme (light or dark, depending on the background), so Material
 * components use the same colors.
 */
@Composable
fun DroidMentorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    colors: AppColors? = null,
    content: @Composable () -> Unit
) {
    val appColors = colors ?: systemAppColors(darkTheme, dynamicColor)
    val scheme = remember(appColors) { appColors.toColorScheme() }

    // Match the status bar and navigation bar icons to the background (light or dark).
    // TODO set edge to edge and verify

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Builds the app colors from a Material color scheme (e.g. the dynamic one).
 * The scheme's primary becomes the top glow, so it is also the default accent.
 */
fun ColorScheme.toAppColors() = AppColors(
    top = GlowSpec(primary.vivid()),
    bottom = GlowSpec(tertiary.vivid()),
    background = surfaceContainerLowest,
)

/**
 * Builds a complete Material color scheme from the app colors, so no component
 * falls back to the baseline Material palette.
 */
private fun AppColors.toColorScheme(): ColorScheme {
    val base = if (isLight) lightColorScheme() else darkColorScheme()

    // Higher containers move slightly towards the text color
    // (darker on light themes, lighter on dark ones).
    fun raised(fraction: Float) = lerp(surface, textPrimary, fraction)

    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentContainer,
        onPrimaryContainer = textPrimary,
        inversePrimary = accent,

        secondary = bottomAccent,
        onSecondary = bottomAccent.contentColor(),
        secondaryContainer = lerp(surface, bottomAccent, 0.2f),
        onSecondaryContainer = textPrimary,

        tertiary = topAccent,
        onTertiary = topAccent.contentColor(),
        tertiaryContainer = lerp(surface, topAccent, 0.2f),
        onTertiaryContainer = textPrimary,

        background = background,
        onBackground = textPrimary,

        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = raised(0.06f),
        onSurfaceVariant = textSecondary,
        surfaceTint = accent,
        inverseSurface = textPrimary,
        inverseOnSurface = background,

        surfaceContainerLowest = background,
        surfaceContainerLow = lerp(background, surface, 0.5f),
        surfaceContainer = surface,
        surfaceContainerHigh = raised(0.04f),
        surfaceContainerHighest = raised(0.08f),

        outline = border,
        outlineVariant = borderSubtle,
    )
}
