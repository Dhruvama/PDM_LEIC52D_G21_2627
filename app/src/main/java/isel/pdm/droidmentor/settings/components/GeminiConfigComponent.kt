package isel.pdm.droidmentor.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.settings.ApiKeyRules
import isel.pdm.droidmentor.domain.settings.GeminiModel
import isel.pdm.droidmentor.domain.settings.MentorSettings
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val GEMINI_CONFIG_COMPONENT_TAG = "gemini_config_component"
const val API_KEY_FIELD_TAG = "api_key_field"

/**
 * Card with the Gemini API key and the model choice.
 *
 * @param mentor Current mentor settings.
 * @param onMentorSettingsChange Called with the new settings when the key or the model change.
 * @param onGetApiKey Called when the user wants to create a key.
 */
@Composable
fun GeminiConfigComponent(
    mentor: MentorSettings,
    onMentorSettingsChange: (MentorSettings) -> Unit,
    onGetApiKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    SectionCard(
        title = stringResource(R.string.settings_gemini_title),
        modifier = modifier.testTag(GEMINI_CONFIG_COMPONENT_TAG),
    ) {
        ApiKeyField(
            value = mentor.apiKey,
            onValueChange = {
                onMentorSettingsChange(mentor.copy(apiKey = ApiKeyRules.normalize(it)))
            }
        )
        Text(
            stringResource(R.string.settings_gemini_key_note),
            style = MaterialTheme.typography.bodySmall,
            color = c.textSecondary
        )
        TextButton(onClick = onGetApiKey, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.settings_gemini_get_key))
        }

        Label(stringResource(R.string.settings_gemini_model))
        SegmentedPill(
            options = GeminiModel.entries.map { it to it.label() },
            selected = mentor.model,
            onSelect = { onMentorSettingsChange(mentor.copy(model = it)) }
        )
    }
}

/** Glass text field for the key, hidden by default, with a button to show it. */
@Composable
private fun ApiKeyField(value: String, onValueChange: (String) -> Unit) {
    val c = LocalAppColors.current
    var visible by rememberSaveable { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .glass(c, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = c.textPrimary, fontFamily = FontFamily.Monospace),
            cursorBrush = SolidColor(c.accent),
            visualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 14.dp)
                .testTag(API_KEY_FIELD_TAG),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(stringResource(R.string.settings_gemini_key_hint), color = c.textSecondary)
                }
                inner()
            }
        )
        TextButton(onClick = { visible = !visible }) {
            Text(
                stringResource(
                    if (visible) R.string.settings_key_hide else R.string.settings_key_show
                )
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun GeminiConfigEmptyDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        GeminiConfigComponent(
            mentor = MentorSettings(),
            onMentorSettingsChange = { },
            onGetApiKey = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun GeminiConfigFilledLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        GeminiConfigComponent(
            mentor = MentorSettings(apiKey = "AIzaSyExampleKey123", model = GeminiModel.PRO),
            onMentorSettingsChange = { },
            onGetApiKey = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}