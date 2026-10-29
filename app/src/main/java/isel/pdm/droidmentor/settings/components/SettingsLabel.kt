package isel.pdm.droidmentor.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.domain.settings.GeminiModel
import isel.pdm.droidmentor.domain.settings.MentorLevel

/** Text shown for this level in the Settings screen. */
@Composable
fun MentorLevel.label(): String = stringResource(
    when (this) {
        MentorLevel.BEGINNER -> R.string.settings_level_beginner
        MentorLevel.INTERMEDIATE -> R.string.settings_level_intermediate
        MentorLevel.SENIOR -> R.string.settings_level_senior
    }
)

/** Text shown for this model in the Settings screen. */
@Composable
fun GeminiModel.label(): String = stringResource(
    when (this) {
        GeminiModel.FLASH -> R.string.settings_model_flash
        GeminiModel.PRO -> R.string.settings_model_pro
    }
)