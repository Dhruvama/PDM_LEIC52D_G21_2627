package isel.pdm.droidmentor.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.ui.components.SendButton
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.PresetThemes
import isel.pdm.droidmentor.ui.theme.modifiers.glass
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground
import isel.pdm.droidmentor.ui.theme.toBackground

const val APPEARANCE_COMPONENT_TAG = "appearance_component"

/**
 * Card with the theme preview, the ready-made themes and the light/dark mode.
 *
 * @param draft Colors currently shown.
 * @param onPresetSelect Called when the user picks a ready-made theme.
 * @param onColorsChange Called with the new colors when the mode changes.
 * @param onColorsFinished Called when the user is done changing the colors, to save them.
 */
@Composable
fun AppearenceComponent(
    draft: AppColors,
    onPresetSelect: (AppColors) -> Unit,
    onColorsChange: (AppColors) -> Unit,
    onColorsFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    SectionCard(
        title = stringResource(R.string.settings_appearance_title),
        modifier = modifier.testTag(APPEARANCE_COMPONENT_TAG),
    ) {
        ThemePreview(Modifier.height(220.dp))

        Label(stringResource(R.string.settings_appearance_presets))
        PresetRow(selected = draft, onSelect = onPresetSelect)

        Label(stringResource(R.string.settings_appearance_mode))
        SegmentedPill(
            options = listOf(
                false to stringResource(R.string.settings_appearance_dark),
                true to stringResource(R.string.settings_appearance_light),
            ),
            selected = c.isLight,
            onSelect = { light ->
                onColorsChange(draft.copy(background = draft.background.toBackground(light)))
                onColorsFinished()
            }
        )
    }
}

/** Small mock of the chat screen, so the colors can be seen in context. */
@Composable
fun ThemePreview(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .glowBackground(c)
            .border(1.dp, c.borderSubtle, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChatBubble(stringResource(R.string.settings_preview_user_message), fromUser = true)
        ChatBubble(stringResource(R.string.settings_preview_mentor_message), fromUser = false)

        Spacer(Modifier.weight(1f))

        Row(
            Modifier
                .fillMaxWidth()
                .glass(c, CircleShape)
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.settings_preview_input_hint),
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                modifier = Modifier.weight(1f)
            )
            SendButton(onClick = {}, diameter = 36.dp)
        }
    }
}

@Composable
private fun ColumnScope.ChatBubble(text: String, fromUser: Boolean) {
    val c = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (fromUser) c.onCrossGradient else c.textPrimary,
        modifier = Modifier
            .align(if (fromUser) Alignment.End else Alignment.Start)
            .widthIn(max = 220.dp)
            .then(
                if (fromUser) Modifier.clip(shape).background(c.crossGradient)
                else Modifier.glass(c, shape)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun PresetRow(selected: AppColors, onSelect: (AppColors) -> Unit) {
    val c = LocalAppColors.current
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PresetThemes.forEach { preset ->
            val isSelected = preset == selected
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(preset.background)
                    .background(
                        // The glows over the background, so it shows if the theme is light or dark
                        Brush.linearGradient(
                            listOf(
                                preset.top.glow.copy(alpha = 0.9f),
                                Color.Transparent,
                                preset.bottom.glow.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) c.textPrimary else c.borderSubtle,
                        shape = CircleShape
                    )
                    .clickable(role = Role.RadioButton) { onSelect(preset) }
            )
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun AppearenceComponentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        AppearenceComponent(
            draft = DefaultDarkColors,
            onPresetSelect = { },
            onColorsChange = { },
            onColorsFinished = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun AppearenceComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        AppearenceComponent(
            draft = DefaultLightColors,
            onPresetSelect = { },
            onColorsChange = { },
            onColorsFinished = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}