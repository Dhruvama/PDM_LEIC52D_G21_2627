package isel.pdm.droidmentor.domain.settings

/**
 * Gemini models the user can pick.
 *
 * @property id Id sent to the Gemini API. The display name lives in the app module.
 */
// TODO confirm the models available in the Gemini API when the REST client is built
enum class GeminiModel(val id: String) {
    FLASH("gemini-2.5-flash"),
    PRO("gemini-2.5-pro"),
}