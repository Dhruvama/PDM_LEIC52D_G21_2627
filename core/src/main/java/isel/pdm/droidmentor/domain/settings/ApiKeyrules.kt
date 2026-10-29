package isel.pdm.droidmentor.domain.settings

/** Domain rules for the Gemini API key typed by the user. */
object ApiKeyRules {
    /** The key as it is stored: without leading or trailing whitespace. */
    fun normalize(raw: String): String = raw.trim()

    /** Whether [key] can be sent to Gemini. For now it only has to be non-blank. */
    fun isValid(key: String): Boolean = key.isNotBlank()
}