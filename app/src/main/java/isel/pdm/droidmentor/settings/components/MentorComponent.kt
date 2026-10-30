package isel.pdm.droidmentor.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.settings.MentorLevel
import isel.pdm.droidmentor.domain.settings.MentorSettings
import isel.pdm.droidmentor.ui.theme.DefaultDarkColors
import isel.pdm.droidmentor.ui.theme.DefaultLightColors
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import isel.pdm.droidmentor.ui.theme.LocalAppColors
import isel.pdm.droidmentor.ui.theme.modifiers.glowBackground

const val MENTOR_COMPONENT_TAG = "mentor_component"

/**
 * Card with how the mentor answers: level, code examples and short answers.
 *
 * @param mentor Current mentor settings.
 * @param onMentorSettingsChange Called with the new settings when any option changes.
 */
@Composable
fun MentorComponent(
    mentor: MentorSettings,
    onMentorSettingsChange: (MentorSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.settings_mentor_title),
        modifier = modifier.testTag(MENTOR_COMPONENT_TAG),
    ) {
        Label(stringResource(R.string.settings_mentor_level))
        SegmentedPill(
            options = MentorLevel.entries.map { it to it.label() },
            selected = mentor.level,
            onSelect = { onMentorSettingsChange(mentor.copy(level = it)) }
        )
        SwitchRow(
            title = stringResource(R.string.settings_mentor_code_examples),
            description = stringResource(R.string.settings_mentor_code_examples_desc),
            checked = mentor.codeExamples,
            onCheckedChange = { onMentorSettingsChange(mentor.copy(codeExamples = it)) }
        )
        SwitchRow(
            title = stringResource(R.string.settings_mentor_short_answers),
            description = stringResource(R.string.settings_mentor_short_answers_desc),
            checked = mentor.shortAnswers,
            onCheckedChange = { onMentorSettingsChange(mentor.copy(shortAnswers = it)) }
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorComponentDarkPreview() {
    DroidMentorTheme(colors = DefaultDarkColors) {
        MentorComponent(
            mentor = MentorSettings(),
            onMentorSettingsChange = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun MentorComponentLightPreview() {
    DroidMentorTheme(colors = DefaultLightColors) {
        MentorComponent(
            mentor = MentorSettings(level = MentorLevel.SENIOR, shortAnswers = true),
            onMentorSettingsChange = { },
            modifier = Modifier.glowBackground(LocalAppColors.current).padding(16.dp),
        )
    }
}