package isel.pdm.droidmentor

import android.app.Application
import isel.pdm.droidmentor.data.fakes.FakeConversationRepository
import isel.pdm.droidmentor.domain.history.ConversationRepository
import isel.pdm.droidmentor.screens.settings.AppColorsRepository
import isel.pdm.droidmentor.screens.settings.settingsDataStore

/** Application class: creates the objects shared by the whole app, once. */
class DroidMentorApplication : DependenciesContainer, Application() {

    /** Saved chats. In memory until the Room implementation exists. */
    override val conversationRepository: ConversationRepository by lazy {
        FakeConversationRepository()
    }

    /** Colors chosen by the user. */
    val appColorsRepository: AppColorsRepository by lazy {
        AppColorsRepository(settingsDataStore)
    }
}