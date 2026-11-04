package isel.pdm.droidmentor.screens.titlescreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.pdm.droidmentor.UserTheme
import isel.pdm.droidmentor.screens.about.AboutActivity
import isel.pdm.droidmentor.screens.activechat.ActiveChatActivity
import isel.pdm.droidmentor.screens.chathistory.ChatHistoryActivity
import isel.pdm.droidmentor.screens.settings.SettingsActivity

private const val LOG_TAG = "DroidMentor.TitleActivity"

/**
 * Hosts [TitleScreen]. It has no ViewModel: the screen has no data, only a visual transition.
 */
class TitleActivity : ComponentActivity() {

    companion object {
        /**
         * Navigates to [TitleActivity].
         * @param origin the activity that is currently showing, used to start this one.
         */
        fun navigateFrom(origin: ComponentActivity) {
            origin.startActivity(Intent(origin, TitleActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(LOG_TAG, "onCreate")
        enableEdgeToEdge()
        setContent {
            UserTheme {
                TitleScreen(
                    onStartChat = {
                        ActiveChatActivity.navigateFrom(
                            origin = this@TitleActivity,
                            assembleLogo = true, // starts the particle animation in the chat
                        )
                    },
                    onHistory = { ChatHistoryActivity.navigateFrom(this@TitleActivity) },
                    onSettings = { SettingsActivity.navigateFrom(this@TitleActivity) },
                    onAbout = { AboutActivity.navigateFrom(this@TitleActivity) },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(LOG_TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.v(LOG_TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(LOG_TAG, "onDestroy")
    }
}