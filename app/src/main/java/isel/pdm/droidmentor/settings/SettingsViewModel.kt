package isel.pdm.droidmentor.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.pdm.droidmentor.DroidMentorApplication
import isel.pdm.droidmentor.buildLogTag
import isel.pdm.droidmentor.domain.settings.MentorSettings
import isel.pdm.droidmentor.ui.theme.AppColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Everything the color part of the Settings screen displays.
 *
 * @property colors Colors being edited, or `null` to use the system colors.
 * @property topLinked The top accent follows the top glow.
 * @property bottomLinked The bottom accent follows the bottom glow.
 * @property loaded `false` until the saved colors have been read.
 */
data class SettingsScreenState(
    val colors: AppColors? = null,
    val topLinked: Boolean = true,
    val bottomLinked: Boolean = true,
    val loaded: Boolean = false,
    val mentor: MentorSettings = MentorSettings(),
)

/** ViewModel of the Settings screen. Reads and saves the colors in [repository]. */
class SettingsViewModel(private val repository: AppColorsRepository) : ViewModel() {

    private val logTag = buildLogTag(this::class.java.simpleName)

    /** What the screen shows. Only this ViewModel changes it. */
    var state by mutableStateOf(SettingsScreenState())
        private set

    init {
        viewModelScope.launch {
            val saved = repository.colors.first()
            state = SettingsScreenState(
                colors = saved,
                topLinked = saved?.let { it.top.rawAccent == it.top.glow } ?: true,
                bottomLinked = saved?.let { it.bottom.rawAccent == it.bottom.glow } ?: true,
                loaded = true,
            )
            Log.v(logTag, "colors loaded: ${saved != null}")
        }
    }

    /** Changes the colors being edited, without saving (so the screen updates live). */
    fun changeColors(colors: AppColors) {
        state = state.copy(colors = colors)
    }

    /** Sets whether the top accent follows the top glow. */
    fun changeTopLinked(linked: Boolean) {
        state = state.copy(topLinked = linked)
    }

    /** Sets whether the bottom accent follows the bottom glow. */
    fun changeBottomLinked(linked: Boolean) {
        state = state.copy(bottomLinked = linked)
    }

    /** Saves the colors currently being edited. */
    fun saveColors() {
        val colors = state.colors ?: return
        viewModelScope.launch { repository.save(colors) }
    }

    /** Uses a ready-made theme and saves it. */
    fun applyPreset(preset: AppColors) {
        state = state.copy(
            colors = preset,
            topLinked = preset.top.rawAccent == preset.top.glow,
            bottomLinked = preset.bottom.rawAccent == preset.bottom.glow,
        )
        viewModelScope.launch { repository.save(preset) }
    }

    /** Goes back to the system colors and removes the saved ones. */
    fun useSystemColors() {
        state = state.copy(colors = null, topLinked = true, bottomLinked = true)
        viewModelScope.launch { repository.save(null) }
    }

    fun updateMentorSettings(newSettings: MentorSettings) {
        state = state.copy(mentor = newSettings)
    }

    companion object {
        /** Factory that gives the ViewModel the app's repository. */
        fun factory(app: DroidMentorApplication) = viewModelFactory {
            initializer { SettingsViewModel(app.appColorsRepository) }
        }
    }
}