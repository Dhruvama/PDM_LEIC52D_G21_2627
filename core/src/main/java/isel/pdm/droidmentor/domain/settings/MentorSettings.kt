package isel.pdm.droidmentor.domain.settings

/**
 * Everything the user configures about the mentor. The chat layer builds Gemini requests
 * from it, and persistence will save and load it as one object.
 *
 * @property apiKey The user's Gemini API key (empty if not set yet).
 * @property model Gemini model used for the answers.
 * @property level How deep the answers go.
 * @property codeExamples Whether answers include Kotlin/Compose code examples.
 * @property shortAnswers Whether answers go straight to the point.
 */
data class MentorSettings(
    val apiKey: String = "",
    val model: GeminiModel = GeminiModel.FLASH,
    val level: MentorLevel = MentorLevel.INTERMEDIATE,
    val codeExamples: Boolean = true,
    val shortAnswers: Boolean = false,
) {
    /** `true` when [apiKey] can be used to call Gemini. */
    val hasValidApiKey: Boolean
        get() = ApiKeyRules.isValid(apiKey)
}