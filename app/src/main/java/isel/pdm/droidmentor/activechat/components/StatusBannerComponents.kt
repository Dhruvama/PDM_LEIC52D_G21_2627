package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.ColorPair
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val OFFLINE_BANNER_TAG = "offline_banner"
const val ERROR_BANNER_TAG = "error_banner"
const val ERROR_RETRY_BUTTON_TAG = "error_retry_button"
const val ERROR_DISMISS_BUTTON_TAG = "error_dismiss_button"

/**
 * Background and content colors of a banner.
 *
 * The banners use fixed colors, not the theme's: amber for offline (a theme accent can be green,
 * which reads as "connected") and red for errors. To stay visible whatever the user's theme
 * (even a red or yellow one), each banner has the opposite lightness of the background:
 * a light banner on a dark theme, a dark banner on a light theme. The theme's backgrounds are
 * always very dark or very light (see BackgroundLimits), so this contrast holds for every theme.
 */
private data class BannerColors(val container: Color, val content: Color)

private val OfflineOnDark = BannerColors(container = Color(0xFFFFD27A), content = Color(0xFF3D2E00))
private val OfflineOnLight = BannerColors(container = Color(0xFF6B4E00), content = Color(0xFFFFF1CC))
private val ErrorOnDark = BannerColors(container = Color(0xFFF2B8B5), content = Color(0xFF601410))
private val ErrorOnLight = BannerColors(container = Color(0xFFB3261E), content = Color.White)

/** Colors of the [OfflineBannerComponent] for the current theme. */
@Composable
private fun offlineBannerColors(): BannerColors =
    if (LocalAppColors.current.isLight) OfflineOnLight else OfflineOnDark

/** Colors of the [ErrorBannerComponent] for the current theme. */
@Composable
private fun errorBannerColors(): BannerColors =
    if (LocalAppColors.current.isLight) ErrorOnLight else ErrorOnDark

/**
 * Full-width banner telling the user there is no connection: the chat can be read,
 * but new questions are paused until the device reconnects.
 *
 * @param modifier Modifier applied to the banner.
 */
@Composable
fun OfflineBannerComponent(modifier: Modifier = Modifier) {
    val colors = offlineBannerColors()
    val containerColor = colors.container
    val contentColor = colors.content

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor.copy(alpha = BANNER_ALPHA))
            .padding(12.dp)
            .testTag(OFFLINE_BANNER_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.offline_banner_message),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

/**
 * Full-width banner showing an error, with a "Retry" button and a dismiss button.
 *
 * @param message Error text, already readable by the user. It is shown as is.
 * @param onRetryClick Called when the user taps "Retry".
 * @param onDismissClick Called when the user taps the dismiss button.
 * @param modifier Modifier applied to the banner.
 */
@Composable
fun ErrorBannerComponent(
    message: String,
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = errorBannerColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container.copy(alpha = BANNER_ALPHA))
            .padding(12.dp)
            .testTag(ERROR_BANNER_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = colors.content,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.content,
            modifier = Modifier.weight(1f),
        )
        ComponentRetryButton(onClick = onRetryClick)
        ComponentDismissButton(onClick = onDismissClick)
    }
}

/**
 * "Retry" text button of the [ErrorBannerComponent].
 *
 * @param onClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
private fun ComponentRetryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = errorBannerColors().content,
        ),
        modifier = modifier.testTag(ERROR_RETRY_BUTTON_TAG),
    ) {
        Text(
            text = stringResource(R.string.error_banner_retry),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Close icon button that dismisses the [ErrorBannerComponent].
 *
 * @param onClick Called when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
private fun ComponentDismissButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag(ERROR_DISMISS_BUTTON_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.error_banner_dismiss),
            tint = errorBannerColors().content,
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview
@Composable
private fun OfflineBannerComponentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(vertical = 16.dp)) {
            OfflineBannerComponent()
        }
    }
}

@Preview
@Composable
private fun OfflineBannerComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(vertical = 16.dp)) {
            OfflineBannerComponent()
        }
    }
}

@Preview
@Composable
private fun ErrorBannerComponentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(vertical = 16.dp)) {
            ErrorBannerComponent(
                message = previewErrorMessage,
                onRetryClick = { },
                onDismissClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun ErrorBannerComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(vertical = 16.dp)) {
            ErrorBannerComponent(
                message = previewErrorMessage,
                onRetryClick = { },
                onDismissClick = { },
            )
        }
    }
}

// Both banners on a red and on a yellow theme: they must still stand out from the glow.
@Preview
@Composable
private fun StatusBannersRedThemePreview() {
    DroidMentorTheme(colors = previewRedTheme) {
        StatusBannersPreviewContent()
    }
}

@Preview
@Composable
private fun StatusBannersYellowThemePreview() {
    DroidMentorTheme(colors = previewYellowTheme) {
        StatusBannersPreviewContent()
    }
}

// The buttons are drawn on the banner's color, as in the real banner.
@Preview
@Composable
private fun ComponentRetryButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.background(errorBannerColors().container).padding(16.dp)) {
            ComponentRetryButton(onClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentDismissButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.background(errorBannerColors().container).padding(16.dp)) {
            ComponentDismissButton(onClick = { })
        }
    }
}

@Composable
private fun StatusBannersPreviewContent() {
    Column(
        modifier = Modifier
            .glowBackground(LocalAppColors.current)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OfflineBannerComponent()
        ErrorBannerComponent(
            message = previewErrorMessage,
            onRetryClick = { },
            onDismissClick = { },
        )
    }
}

private const val previewErrorMessage = "Too many requests (429). Try again in a moment."

private val previewRedTheme = AppColors(
    top = ColorPair(Color(0xFFE53935)),
    bottom = ColorPair(Color(0xFFFF7043)),
    background = Color(0xFF0B0606),
)

private val previewYellowTheme = AppColors(
    top = ColorPair(Color(0xFFFFC107)),
    bottom = ColorPair(Color(0xFFFFEB3B)),
    background = Color(0xFFFAF8EF),
)