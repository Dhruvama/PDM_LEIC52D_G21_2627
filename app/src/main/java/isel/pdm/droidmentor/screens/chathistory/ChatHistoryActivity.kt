package isel.pdm.droidmentor.screens.chathistory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.pdm.droidmentor.DependenciesContainer
import isel.pdm.droidmentor.UserTheme
import isel.pdm.droidmentor.screens.activechat.ActiveChatActivity
import isel.pdm.droidmentor.buildLogTag

class ChatHistoryActivity : ComponentActivity() {

    private val viewModel: ChatHistoryScreenViewModel by viewModels {
        ChatHistoryScreenViewModel.factory(application as DependenciesContainer)
    }
    private val logTag = buildLogTag(this::class.java.simpleName)

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
            UserTheme {
                ChatHistoryScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onOpenChat = {
                        Log.v(logTag, "Chat ${it.id} clicked")
                        // TODO: open the saved chat
                    },
                    // TODO resolver ciclos na navegação (Chat → Histórico → Chat → Histórico...)
                    onNewChat = { ActiveChatActivity.navigateFrom(this@ChatHistoryActivity) },
                )
            }
        }
    }
}