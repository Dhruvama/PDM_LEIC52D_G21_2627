package isel.pdm.droidmentor

import isel.pdm.droidmentor.domain.chat.MessageRepository
import isel.pdm.droidmentor.domain.history.ConversationRepository

/**
 * The application's dependencies, shared by every screen.
 *
 * Implemented by [DroidMentorApplication], which creates each instance once. ViewModels receive
 * it in their `factory` and take only the contracts they need.
 */
interface DependenciesContainer {

    /** The saved chats, as shown in the Chat History screen. */
    val conversationRepository: ConversationRepository

    /** The messages of each saved chat. */
    val messageRepository: MessageRepository
}