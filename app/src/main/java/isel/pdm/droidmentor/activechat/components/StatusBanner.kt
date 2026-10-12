package isel.pdm.droidmentor.activechat.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

const val OFFLINE_BANNER_TAG = "offline_banner"
const val ERROR_BANNER_TAG = "error_banner"
const val ERROR_RETRY_BUTTON_TAG = "error_retry_button"
const val ERROR_DISMISS_BUTTON_TAG = "error_dismiss_button"

/**
 * Full-width banner telling the user there is no connection: the chat can be read,
 * but new questions are paused until the device reconnects.
 *
 * @param modifier Modifier applied to the banner.
 */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = BANNER_ALPHA))
            .padding(12.dp)
            .testTag(OFFLINE_BANNER_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.offline_banner_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
fun ErrorBanner(
    message: String,
    onRetryClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = BANNER_ALPHA))
            .padding(12.dp)
            .testTag(ERROR_BANNER_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onRetryClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.testTag(ERROR_RETRY_BUTTON_TAG),
        ) {
            Text(
                text = stringResource(R.string.error_banner_retry),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        IconButton(
            onClick = onDismissClick,
            modifier = Modifier.testTag(ERROR_DISMISS_BUTTON_TAG),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.error_banner_dismiss),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    DroidMentorTheme(dynamicColor = false) {
        OfflineBanner()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorBannerPreview() {
    DroidMentorTheme(dynamicColor = false) {
        ErrorBanner(
            message = previewErrorMessage,
            onRetryClick = { },
            onDismissClick = { },
        )
    }
}

private const val previewErrorMessage = "Too many requests (429). Try again in a moment."