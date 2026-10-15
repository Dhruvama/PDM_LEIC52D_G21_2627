package isel.pdm.droidmentor.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import androidx.core.net.toUri

private const val REPOSITORY_URL = "https://github.com/Dhruvama/PDM_LEIC52D_G21_2627"

class AboutActivity : ComponentActivity() {
    private val logTag = "AboutActivity"
    companion object {
        /**
         * Navigates to [AboutActivity].
         * @param origin the activity that is currently showing, used to start this one.
         */
        fun navigateFrom(origin: ComponentActivity){
            val msg = Intent(origin, AboutActivity::class.java)
            origin.startActivity(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                AboutScreen(
                    onNavigateToGitHub = ::navigateToGitHub,
                    onBack = { finish() }
                )
            }
        }


    }

    private fun navigateToGitHub() {
        val intent = Intent(Intent.ACTION_VIEW, REPOSITORY_URL.toUri())
        intent.resolveActivity(packageManager)?.let {
            startActivity(intent)
        }
    }

}