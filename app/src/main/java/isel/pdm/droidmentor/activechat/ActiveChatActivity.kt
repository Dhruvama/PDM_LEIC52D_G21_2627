package isel.pdm.droidmentor.activechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

class ActiveChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ActiveChatScreen()
            }
        }
    }
}