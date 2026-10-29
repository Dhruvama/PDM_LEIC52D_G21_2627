package isel.pdm.droidmentor.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import isel.pdm.droidmentor.ui.theme.AppColors
import isel.pdm.droidmentor.ui.theme.ColorPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** The app's DataStore file. Declared once per process. */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val TOP_GLOW = intPreferencesKey("colors_top_glow")
private val TOP_ACCENT = intPreferencesKey("colors_top_accent")
private val BOTTOM_GLOW = intPreferencesKey("colors_bottom_glow")
private val BOTTOM_ACCENT = intPreferencesKey("colors_bottom_accent")
private val BACKGROUND = intPreferencesKey("colors_background")

/**
 * Stores the colors chosen by the user. Each color is saved as an ARGB Int.
 *
 * @param dataStore where the colors are kept.
 */
class AppColorsRepository(private val dataStore: DataStore<Preferences>) {

    /** The saved colors, or `null` when the user has not chosen any (use the system colors). */
    val colors: Flow<AppColors?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val topGlow = prefs[TOP_GLOW]
            val topAccent = prefs[TOP_ACCENT]
            val bottomGlow = prefs[BOTTOM_GLOW]
            val bottomAccent = prefs[BOTTOM_ACCENT]
            val background = prefs[BACKGROUND]
            // If any key is missing the saved colors are incomplete: use the system ones.
            if (topGlow == null || topAccent == null || bottomGlow == null ||
                bottomAccent == null || background == null
            ) {
                null
            } else {
                AppColors(
                    top = ColorPair(Color(topGlow), Color(topAccent)),
                    bottom = ColorPair(Color(bottomGlow), Color(bottomAccent)),
                    background = Color(background),
                )
            }
        }

    /** Saves [colors]. `null` removes the saved colors, so the system colors are used again. */
    suspend fun save(colors: AppColors?) {
        dataStore.edit { prefs ->
            if (colors == null) {
                prefs.remove(TOP_GLOW)
                prefs.remove(TOP_ACCENT)
                prefs.remove(BOTTOM_GLOW)
                prefs.remove(BOTTOM_ACCENT)
                prefs.remove(BACKGROUND)
            } else {
                prefs[TOP_GLOW] = colors.top.glow.toArgb()
                prefs[TOP_ACCENT] = colors.top.rawAccent.toArgb()
                prefs[BOTTOM_GLOW] = colors.bottom.glow.toArgb()
                prefs[BOTTOM_ACCENT] = colors.bottom.rawAccent.toArgb()
                prefs[BACKGROUND] = colors.background.toArgb()
            }
        }
    }
}