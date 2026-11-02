package storage.repository

import kotlinx.coroutines.flow.Flow
import model.Author
import model.Conversation
import model.Message
import kotlin.uuid.Uuid

interface ConversationRepository {
    val conversations: Flow<List<Conversation>>
    fun observeMessages(conversationId: Uuid): Flow<List<Message>>
    suspend fun getConversation(conversationId: Uuid): Conversation?
    suspend fun appendUserMessage(conversationId: Uuid?, text: String): Uuid
    suspend fun appendMessage(conversationId: Uuid, role: Author, text: String): Message
    suspend fun getMessagesPage(conversationId: Uuid, limit: Int, offset: Int): List<Message>
    suspend fun deleteConversation(conversationId: Uuid)
}
