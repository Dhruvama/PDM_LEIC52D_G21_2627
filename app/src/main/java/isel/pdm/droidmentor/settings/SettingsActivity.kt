package isel.pdm.droidmentor.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
private const val API_KEY_URL = "https://aistudio.google.com/apikey"

class SettingsActivity : ComponentActivity() {
    private val logTag = "SettingsActivity"
    companion object {
        fun navigateFrom(origin: ComponentActivity) {
            val msg = Intent(origin, SettingsActivity::class.java)
            origin.startActivity(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            // The screen applies the theme itself, so the colors being edited show live
            SettingsScreen(
                onGetApiKey = { openApiKeyPage() },
                onBack = { finish() }
            )
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

private fun Context.openApiKeyPage() {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, API_KEY_URL.toUri()))
    } catch (e: Exception) {
        Log.e("SettingsActivity", "Couldnt open the API key page: ${e.message}")
    }
}