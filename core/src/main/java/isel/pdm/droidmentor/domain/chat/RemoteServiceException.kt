package isel.pdm.droidmentor.domain.chat

/**
 * Failures of the remote AI service (Gemini) as the domain sees them.
 *
 * Contracts that talk to the service throw only these exceptions: the adapters translate HTTP
 * statuses and network errors into one of them. Being sealed, a `when` over them is exhaustive,
 * so the UI is forced to choose a message for every case.
 *
 * Messages carry only technical detail (e.g. the HTTP status) and never the API key.
 */
sealed class RemoteServiceException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

/** The API key is missing, invalid or not allowed to use the service (HTTP 400 with an invalid key, 401, 403). */
class InvalidApiKeyException(message: String? = null, cause: Throwable? = null) :
    RemoteServiceException(message, cause)

/**
 * Too many requests, or the key's quota was exhausted (HTTP 429).
 *
 * @property retryAfterSeconds How long the service asked to wait before trying again,
 * or `null` if it did not say.
 */
class RateLimitedException(
    message: String? = null,
    cause: Throwable? = null,
    val retryAfterSeconds: Long? = null,
) : RemoteServiceException(message, cause)

/** The service failed or is overloaded (HTTP 5xx). Trying again later may work. */
class ServiceUnavailableException(message: String? = null, cause: Throwable? = null) :
    RemoteServiceException(message, cause)

/** The service took too long to answer. */
class RequestTimeoutException(message: String? = null, cause: Throwable? = null) :
    RemoteServiceException(message, cause)

/** The service could not be reached: the device is offline or the network failed. */
class NoConnectivityException(message: String? = null, cause: Throwable? = null) :
    RemoteServiceException(message, cause)

/** The service answered in a way the app does not expect (any other HTTP status). */
class UnexpectedServiceException(message: String? = null, cause: Throwable? = null) :
    RemoteServiceException(message, cause)