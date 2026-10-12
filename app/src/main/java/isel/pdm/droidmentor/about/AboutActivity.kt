package isel.pdm.droidmentor.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.pdm.droidmentor.ui.theme.DroidMentorTheme
import androidx.core.net.toUri

private const val REPOSITORY_URL = "https://github.com/Dhruvama/PDM_LEIC52D_G21_2627"

class AboutActivity : ComponentActivity() {
    private val logTag = "AboutActivity"
    companion object {
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
                    onNavigateToGitHub = { openRepository() },
                    onBack = { finish() }
                )
            }
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

private fun android.content.Context.openRepository() {
    try {
        val intent = Intent(Intent.ACTION_VIEW, REPOSITORY_URL.toUri())
        startActivity(intent)
    } catch (e: Exception) {
        Log.e("AboutActivity", "Couldnt navigate to the repository: ${e.message}")
    }
}