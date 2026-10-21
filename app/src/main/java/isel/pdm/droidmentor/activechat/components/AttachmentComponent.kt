package isel.pdm.droidmentor.activechat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val ATTACHMENT_PREVIEW_TAG = "attachment_preview"
const val REMOVE_ATTACHMENT_BUTTON_TAG = "remove_attachment_button"

/**
 * Thumbnail of the image attached in the composer and not yet sent,
 * with a small button on its top-end corner to remove it.
 *
 * @param imageUri The attached image. Not loaded yet: a placeholder is shown for now.
 * @param onRemoveClick Called when the user taps the remove button.
 * @param modifier Modifier applied to the preview.
 */
@Composable
fun AttachmentComponent(
    imageUri: Uri,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The thumbnail is padded on top and end so the remove button can sit on its corner
    // while staying inside this Box: taps outside a parent's bounds are not delivered.
    Box(modifier = modifier.testTag(ATTACHMENT_PREVIEW_TAG)) {
        ComponentThumbnail(
            imageUri = imageUri,
            modifier = Modifier.padding(top = 8.dp, end = 8.dp),
        )
        ComponentRemoveAttachmentButton(
            onClick = onRemoveClick,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

/**
 * The 72.dp square thumbnail. For now a placeholder with the attach icon.
 */
@Composable
private fun ComponentThumbnail(imageUri: Uri, modifier: Modifier = Modifier) {
    // TODO: load imageUri with Coil (image step)
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_attach_file),
            contentDescription = null,
            tint = colors.textSecondary,
        )
    }
}

/**
 * Small 22.dp round button with a close icon that removes the attachment.
 */
@Composable
private fun ComponentRemoveAttachmentButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(colors.textPrimary)
            .clickable(onClick = onClick, role = Role.Button)
            .testTag(REMOVE_ATTACHMENT_BUTTON_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.attachment_remove),
            tint = colors.background,
            modifier = Modifier.size(14.dp),
        )
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview
@Composable
private fun AttachmentComponentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            AttachmentComponent(imageUri = Uri.EMPTY, onRemoveClick = { })
        }
    }
}

@Preview
@Composable
private fun AttachmentComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            AttachmentComponent(imageUri = Uri.EMPTY, onRemoveClick = { })
        }
    }
}

@Preview
@Composable
private fun ComponentThumbnailPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentThumbnail(imageUri = Uri.EMPTY)
        }
    }
}

@Preview
@Composable
private fun ComponentRemoveAttachmentButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        Box(Modifier.glowBackground(LocalAppColors.current).padding(16.dp)) {
            ComponentRemoveAttachmentButton(onClick = { })
        }
    }
}