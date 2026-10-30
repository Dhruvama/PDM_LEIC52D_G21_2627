package isel.pdm.droidmentor.settings

import isel.pdm.droidmentor.domain.settings.MentorSettings
import isel.pdm.droidmentor.ui.theme.AppColors

/**
 * Everything the user can do on the Settings screen.
 *
 * Grouped in one interface so [SettingsScreenContent] and its previews don't repeat
 * every callback. The screen root ([SettingsScreen]) creates one implementation that
 * forwards each action to the ViewModel or to navigation.
 *
 * Actions:
 * - [onColorsChange]: the colors being edited changed (the screen updates live, nothing is saved yet).
 * - [onColorsFinished]: the user finished editing, so the colors are saved.
 * - [onTopLinkedChange]: the user linked or unlinked the top accent from the top glow.
 * - [onBottomLinkedChange]: the user linked or unlinked the bottom accent from the bottom glow.
 * - [onPresetSelect]: the user picked a ready-made theme (it is saved right away).
 * - [onUseSystemColors]: the user went back to the system colors.
 * - [onGetApiKey]: the user wants to create a Gemini API key.
 * - [onBack]: the user tapped the back button.
 */
interface SettingsActions {
    fun onColorsChange(colors: AppColors)
    fun onColorsFinished()
    fun onTopLinkedChange(linked: Boolean)
    fun onBottomLinkedChange(linked: Boolean)
    fun onPresetSelect(preset: AppColors)
    fun onUseSystemColors()
    fun onGetApiKey()
    fun onMentorSettingsChange(settings: MentorSettings)
    fun onBack()
}