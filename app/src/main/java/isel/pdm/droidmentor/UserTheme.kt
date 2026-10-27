package isel.pdm.droidmentor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

/**
 * [isel.pdm.droidmentor.ui.theme.DroidMentorTheme] with the colors the user saved in Settings
 * (the system colors if there are none). Updates live when they change.
 */
@Composable
fun UserTheme(content: @Composable () -> Unit) {
    val app = LocalContext.current.applicationContext as DroidMentorApplication
    val colors by app.appColorsRepository.colors.collectAsStateWithLifecycle(initialValue = null)
    DroidMentorTheme(colors = colors, content = content)
}