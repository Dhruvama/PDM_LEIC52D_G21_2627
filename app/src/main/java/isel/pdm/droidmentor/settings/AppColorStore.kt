package isel.pdm.droidmentor.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import isel.pdm.droidmentor.ui.theme.AppColors

/**
 * Colors chosen by the user, shared by all screens.
 * Null means "use the system colors". Kept in memory only.
 */
object AppColorsStore {
    var current: AppColors? by mutableStateOf(null)
}