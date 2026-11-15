package isel.pdm.droidmentor.domain

/**
 * Failures of local storage (Room, DataStore) as the domain sees them.
 *
 * Repository contracts throw only these exceptions (or emit them from their flows): the
 * implementations translate the storage errors they know into one of them and let any other
 * exception through, since it is a bug.
 */
sealed class PersistenceException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

/** A temporary failure (e.g. the database is locked). Trying again may work. */
class RecoverablePersistenceException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)

/** A failure that trying again will not fix (e.g. corrupted data, disk full). */
class UnrecoverablePersistenceException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)