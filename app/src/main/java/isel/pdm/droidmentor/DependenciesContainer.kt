package isel.pdm.droidmentor

import isel.pdm.droidmentor.domain.history.ConversationRepository

/**
 * The application's dependencies
 */
interface DependenciesContainer {
    val conversationRepository: ConversationRepository
}