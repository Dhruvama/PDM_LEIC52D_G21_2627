package isel.pdm.droidmentor.activechat

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.pdm.droidmentor.R
import isel.pdm.droidmentor.buildLogTag
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme

/**
 * Hosts the Active Chat screen. Opens and closes with a fade, so the transition
 * from and back to the Title screen looks continuous.
 */
class ActiveChatActivity : ComponentActivity() {

    private val viewModel by viewModels<ActiveChatScreenViewModel>()
    private val logTag = buildLogTag(this::class.java.simpleName)

    companion object {
        private const val EXTRA_ASSEMBLE_LOGO = "assembleLogo"

        /** Opens the chat. assembleLogo = true makes the particle logo assemble from a cloud (Title screen transition). */
        fun navigateFrom(origin: ComponentActivity, assembleLogo: Boolean = false) {
            val msg = Intent(origin, ActiveChatActivity::class.java)
            msg.putExtra(EXTRA_ASSEMBLE_LOGO, assembleLogo)
            // The chat fades in over the origin, which stays fully visible underneath.
            val fade = ActivityOptions.makeCustomAnimation(origin, android.R.anim.fade_in, R.anim.hold)
            origin.startActivity(msg, fade.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()

        // Closing: the chat fades out over the screen behind it, which stays fully visible.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.hold, android.R.anim.fade_out)
        }

        // Only the first time: after a rotation the logo must not re-assemble.
        val assembleLogo = savedInstanceState == null && intent.getBooleanExtra(EXTRA_ASSEMBLE_LOGO, false)

        setContent {
            DroidMentorTheme {
                ActiveChatScreen(
                    viewModel = viewModel,
                    assembleLogo = assembleLogo,
                    onBack = { finish() },
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Before API 34 there is no overrideActivityTransition, so the closing fade is set here.
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.hold, android.R.anim.fade_out)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(logTag, "onStart() on ${hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        Log.v(logTag, "onStop() on ${hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(logTag, "onDestroy() on ${hashCode()}")
    }
}