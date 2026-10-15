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
 * @param isInputEnabled `false` when offline or waiting for a reply; disables the text field,
 * attach and camera buttons, and shows the offline placeholder.
 * @param isSendEnabled Whether the send button can be tapped. Computed by the caller.
 * @param onAttachClick Called when the user taps the attach button.
 * @param onCameraClick Called when the user taps the camera button.
 * @param onSendClick Called when the user taps the send button.
 * @param modifier Modifier applied to the composer card.
 */
@Composable
fun ChatComposer(
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
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .frostedGlass(c, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (attachedImage != null) {
            AttachmentPreview(imageUri = attachedImage, onRemoveClick = onRemoveAttachment)
        }
        InputRow(
            text = text,
            onTextChange = onTextChange,
            isInputEnabled = isInputEnabled,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ComposerIconButton(
                painter = painterResource(R.drawable.ic_attach_file),
                contentDescription = stringResource(R.string.composer_attach),
                enabled = isInputEnabled,
                onClick = onAttachClick,
                testTag = ATTACH_BUTTON_TAG,
            )
            ComposerIconButton(
                painter = painterResource(R.drawable.ic_photo_camera),
                contentDescription = stringResource(R.string.composer_camera),
                enabled = isInputEnabled,
                onClick = onCameraClick,
                testTag = CAMERA_BUTTON_TAG,
            )
            Spacer(modifier = Modifier.weight(1f))
            SendButton(enabled = isSendEnabled, onClick = onSendClick)
        }
    }
}

/**
 * Sparkle icon followed by the text field, which grows from 2 to 5 lines.
 */
@Composable
private fun InputRow(
    text: String,
    onTextChange: (String) -> Unit,
    isInputEnabled: Boolean,
) {
    val c = LocalAppColors.current
    val placeholder = if (isInputEnabled) {
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
            tint = c.textSecondary,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(18.dp),
        )
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            enabled = isInputEnabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = c.textPrimary,
            ),
            cursorBrush = SolidColor(c.accent),
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
                            color = c.textSecondary,
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
private fun ComposerIconButton(
    painter: Painter,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val c = LocalAppColors.current
    val iconTint = if (enabled) c.textPrimary else c.textSecondary

    Box(
        modifier = Modifier
            .size(44.dp)
            .frostedGlass(c, CircleShape),
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
 * flat surface color when disabled.
 */
@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val c = LocalAppColors.current
    val backgroundModifier = if (enabled) {
        Modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = c.accent,
                spotColor = c.accent,
            )
            .background(brush = c.crossGradient, shape = CircleShape)
    } else {
        Modifier.background(color = c.surface, shape = CircleShape)
    }

    val iconTint = if (enabled) c.onCrossGradient else c.textSecondary

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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.composer_send),
                tint = iconTint,
            )
        }
    }
}

//==================================== TESTS/PREVIEWS ====================================//

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChatComposerEmptyPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatComposer(
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
private fun ChatComposerWithTextPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatComposer(
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
private fun ChatComposerMultiLinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatComposer(
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
private fun ChatComposerOfflinePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatComposer(
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
private fun ChatComposerWithAttachmentPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ChatComposer(
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
private fun ChatComposerLightThemePreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        ChatComposer(
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
private fun ChatComposerInteractivePreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        var text by remember { mutableStateOf("") }
        ChatComposer(
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
private fun InputRowPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        InputRow(text = previewShortText, onTextChange = { }, isInputEnabled = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun ComposerIconButtonPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        ComposerIconButton(
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
private fun SendButtonEnabledPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        SendButton(enabled = true, onClick = { })
    }
}

@Preview(showBackground = true)
@Composable
private fun SendButtonDisabledPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        SendButton(enabled = false, onClick = { })
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