package isel.pdm.droidmentor

import android.app.Application
import isel.pdm.droidmentor.settings.AppColorsRepository
import isel.pdm.droidmentor.settings.settingsDataStore

/** Application class: creates the objects shared by the whole app, once. */
class DroidMentorApplication : Application() {
    /** Colors chosen by the user. */
    val appColorsRepository: AppColorsRepository by lazy {
        AppColorsRepository(settingsDataStore)
    }
}