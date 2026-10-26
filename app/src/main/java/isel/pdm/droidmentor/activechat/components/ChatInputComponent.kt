package isel.pdm.droidmentor.activechat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.frostedGlass

const val MESSAGE_INPUT_TAG = "message_input"
const val ATTACH_BUTTON_TAG = "attach_button"
const val CAMERA_BUTTON_TAG = "camera_button"
const val SEND_BUTTON_TAG = "send_button"
const val EDITING_HEADER_TAG = "editing_header"
const val CANCEL_EDIT_BUTTON_TAG = "cancel_edit_button"

/**
 * The input bar at the bottom of the Active Chat screen: a text field with a sparkle icon,
 * attach and camera buttons, and a round send button.
 *
 * It is stateless: it shows what it receives and reports user actions through the callbacks.
 * Window insets and keyboard padding are handled by the screen, not here.
 *
 * @param text Current text of the input.
 * @param onTextChange Called with the new text whenever the user types.
 * @param attachedImage Image attached and not yet sent, shown as a thumbnail above the text field,
 * or `null` if there is none.
 * @param onRemoveAttachment Called when the user taps the thumbnail's remove button.
 * @param isInputEnabled `false` when offline; disables the text field, attach and camera buttons,
 * and shows the offline placeholder. Stays `true` while waiting for a reply.
 * @param isSendEnabled Whether the send button can be tapped. Computed by the caller; `false`
 * while waiting for a reply.
 * @param onAttachClick Called when the user taps the attach button.
 * @param onCameraClick Called when the user taps the camera button.
 * @param onSendClick Called when the user taps the send button.
 * @param modifier Modifier applied to the composer card.
 * @param isEditing `true` while the user is editing a sent message: shows the editing header
 * on top and a check icon on the send button. [text] and [attachedImage] then hold the
 * edited message's text and image, and can be changed as usual.
 * @param onCancelEdit Called when the user taps the editing header's cancel button.
 */
@Composable
fun ChatInputComponent(
    text: String,
    onTextChange: (String) -> Unit,
    attachedImage: Uri?,
    onRemoveAttachment: () -> Unit,
    isInputEnabled: Boolean,
    isSendEnabled: Boolean,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onCancelEdit: () -> Unit = {},
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .frostedGlass(colors, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (isEditing) {
            ComponentEditingHeader(onCancelEdit = onCancelEdit)
        }
        if (attachedImage != null) {
            AttachmentComponent(imageUri = attachedImage, onRemoveClick = onRemoveAttachment)
        }
        ComponentInputRow(
            text = text,
            onTextChange = onTextChange,
            isInputEnabled = isInputEnabled,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ComponentAttachIconButton(
                painter = painterResource(R.drawable.ic_attach_file),
                contentDescription = stringResource(R.string.composer_attach),
                enabled = isInputEnabled,
                onClick = onAttachClick,
                testTag = ATTACH_BUTTON_TAG,
            )
            ComponentAttachIconButton(
                painter = painterResource(R.drawable.ic_photo_camera),
                contentDescription = stringResource(R.string.composer_camera),
                enabled = isInputEnabled,
                onClick = onCameraClick,
                testTag = CAMERA_BUTTON_TAG,
            )
            Spacer(modifier = Modifier.weight(1f))
            ComponentSendButton(enabled = isSendEnabled, onClick = onSendClick, isEditing = isEditing)
        }
    }
}

/**
 * Shown on top of the composer while editing a message: an edit icon, the "Editing message"
 * title with a warning that the following messages will be replaced, and a cancel button.
 * A thin divider separates it from the rest of the composer.
 */
@Composable
private fun ComponentEditingHeader(onCancelEdit: () -> Unit) {
    val colors = LocalAppColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EDITING_HEADER_TAG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.composer_editing_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.composer_editing_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            ComponentCancelEditing(onCancelEdit = onCancelEdit)
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
    }
}

/**
 * Icon-only "X" button at the end of the editing header. Tapping it reports through
 * [onCancelEdit] that the user wants to leave edit mode without resending the message.
 */
@Composable
private fun ComponentCancelEditing(onCancelEdit: () -> Unit){
    val colors = LocalAppColors.current
    IconButton(
        onClick = onCancelEdit,
        modifier = Modifier.testTag(CANCEL_EDIT_BUTTON_TAG),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.composer_cancel_editing),
            tint = colors.textSecondary,
        )
    }
}

/**
 * Sparkle icon followed by the text field, which grows from 2 to 5 lines.
 */
@Composable
private fun ComponentInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    isInputEnabled: Boolean,
) {
    val colors = LocalAppColors.current
    val placeholder =
        if (isInputEnabled) {
            stringResource(R.string.composer_placeholder)
        } else {
            stringResource(R.string.composer_placeholder_offline)
        }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_auto_awesome),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(18.dp),
        )
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = isInputEnabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = colors.textPrimary,
            ),
            cursorBrush = SolidColor(colors.accent),
            minLines = 2,
            maxLines = 5,
            modifier = Modifier
                .weight(1f)
                .testTag(MESSAGE_INPUT_TAG),
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * A 44.dp round glass button with a 20.dp icon, used for attach and camera.
 */
@Composable
private fun ComponentAttachIconButton(
    painter: Painter,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val colors = LocalAppColors.current
    val iconTint = if (enabled) colors.textPrimary else colors.textSecondary

    Box(
        modifier = Modifier
            .size(44.dp)
            .frostedGlass(colors, CircleShape),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxSize()
                .testTag(testTag),
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Round 52.dp send button: the theme's cross gradient with an accent glow when enabled,
 * flat surface color when disabled. Shows a check icon instead of the send icon when [isEditing].
 */
@Composable
private fun ComponentSendButton(enabled: Boolean, onClick: () -> Unit, isEditing: Boolean = false) {
    val colors = LocalAppColors.current
    val backgroundModifier = if (enabled) {
        Modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = colors.accent,
                spotColor = colors.accent,
            )
            .background(brush = colors.crossGradient, shape = CircleShape)
    } else {
        Modifier.background(color = colors.surface, shape = CircleShape)
    }

    val iconTint = if (enabled) colors.onCrossGradient else colors.textSecondary

    Box(
        modifier = Modifier
            .size(52.dp)
            .then(backgroundModifier),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxSize()
                .testTag(SEND_BUTTON_TAG),
        ) {
            if (isEditing) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.composer_resend_edited),
                    tint = iconTint,
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.composer_send),
                    tint = iconTint,
                )
            }
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentEmptyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = "",
            onTextChange = { },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = false,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentWithTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = previewShortText,
            onTextChange = { },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = true,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentMultiLinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = previewMultiLineText,
            onTextChange = { },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = true,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentOfflinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = "",
            onTextChange = { },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = false,
            isSendEnabled = false,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentWithAttachmentPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = previewAttachmentText,
            onTextChange = { },
            attachedImage = Uri.EMPTY,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = true,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentEditingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatInputComponent(
            text = previewAttachmentText,
            onTextChange = { },
            attachedImage = Uri.EMPTY,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = true,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
            isEditing = true,
            onCancelEdit = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentLightThemePreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ChatInputComponent(
            text = previewShortText,
            onTextChange = { },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = true,
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatInputComponentInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var text by remember { mutableStateOf("") }
        ChatInputComponent(
            text = text,
            onTextChange = { text = it },
            attachedImage = null,
            onRemoveAttachment = { },
            isInputEnabled = true,
            isSendEnabled = text.isNotBlank(),
            onAttachClick = { },
            onCameraClick = { },
            onSendClick = { },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ComponentInputRowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentInputRow(text = previewShortText, onTextChange = { }, isInputEnabled = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentAttachIconButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentAttachIconButton(
            painter = painterResource(R.drawable.ic_attach_file),
            contentDescription = stringResource(R.string.composer_attach),
            enabled = true,
            onClick = { },
            testTag = ATTACH_BUTTON_TAG,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentSendButtonEnabledPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentSendButton(enabled = true, onClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentSendButtonDisabledPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentSendButton(enabled = false, onClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentSendButtonEditingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentSendButton(enabled = true, onClick = { }, isEditing = true)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ComponentEditingHeaderPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentEditingHeader(onCancelEdit = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun ComponentCancelEditingPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComponentCancelEditing(onCancelEdit = { })
    }
}

private const val previewShortText = "How do I use StateFlow?"

private const val previewAttachmentText = "What's wrong with this layout?"

private val previewMultiLineText = """
    I have a ViewModel that loads messages from the network.
    Should I expose them with StateFlow or with mutableStateOf?
    The screen also needs a loading flag and an error message.
    Do I put everything in one state class?
    How do I collect it safely in Compose?
    And what happens on rotation?
    Line seven, to check that the field stops growing.
""".trimIndent()