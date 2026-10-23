package isel.pdm.droidmentor.domain.chat

/**
 * Why the last request to the mentor failed. The screen turns it into a message;
 *  the ViewModel never holds UI text.
 */
enum class FailureReason {
    RATE_LIMITED,
    SERVER_ERROR,
    INVALID_KEY,
    NO_CONNECTION,
    UNKNOWN
}