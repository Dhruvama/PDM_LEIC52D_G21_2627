package isel.pdm.droidmentor.titlescreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.pdm.droidmentor.about.AboutActivity
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

// No shared Logging.kt was visible to me, so the tag is defined locally.
private val logTag = "DroidMentor.TitleActivity"

/**
 * Hosts [TitleScreen]. It has no ViewModel: the screen has no data, only a visual transition.
 */
class TitleActivity : ComponentActivity() {

    companion object {
        /** Starts [TitleActivity] from [origin]. */
        fun navigateFrom(origin: ComponentActivity) {
            origin.startActivity(Intent(origin, TitleActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate")
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                TitleScreen(
                    onStartChat = {
                        // TODO: navigate to Active Chat
                        Log.i(logTag, "Start chat pressed (navigation to Active Chat not wired yet)")
                    },
                    onAbout = {
                        Log.i(logTag, "About pressed (About screen not built yet)")
                        AboutActivity.navigateFrom(this@TitleActivity)
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(logTag, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.v(logTag, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(logTag, "onDestroy")
    }
}