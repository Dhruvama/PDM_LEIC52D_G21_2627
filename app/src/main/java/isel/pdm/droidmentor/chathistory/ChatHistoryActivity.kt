package isel.pdm.droidmentor.chathistory

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

class ChatHistoryActivity : ComponentActivity() {

    companion object {
        /**
         * Navigates to [ChatHistoryActivity].
         * @param origin the activity that is currently showing, used to start this one.
         */
        fun navigateFrom(origin: ComponentActivity) {
            origin.startActivity(Intent(origin, ChatHistoryActivity::class.java))
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ChatHistoryScreen()
            }
        }
    }
}