package storage.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.map
import model.Author
import model.Conversation
import model.Message
import storage.AppDatabase
import storage.entity.ConversationEntity
import storage.entity.MessageEntity
import kotlin.uuid.Uuid

class RoomConversationRepository(private val database: AppDatabase) : ConversationRepository {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    override val conversations = conversationDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override fun observeMessages(conversationId: Uuid) =
        messageDao.observeByConversationId(conversationId.toString()).map { rows -> rows.map { it.toModel() } }

    override suspend fun getConversation(conversationId: Uuid): Conversation? =
        conversationDao.getById(conversationId.toString())?.toModel()

    override suspend fun appendUserMessage(conversationId: Uuid?, text: String): Uuid {
        val content = text.trim()
        require(content.isNotEmpty()) { "A mensagem não pode estar vazia." }
        return database.withTransaction {
            val id = conversationId ?: Uuid.random().also { newId ->
                conversationDao.insert(ConversationEntity(
                    id = newId.toString(),
                    title = if (content.length <= 30) content else content.take(30) + "...",
                    createdAt = System.currentTimeMillis()
                ))
            }
            insertMessage(id, Author.USER, content)
            id
        }
    }

    override suspend fun appendMessage(conversationId: Uuid, role: Author, text: String): Message {
        require(text.isNotBlank()) { "A mensagem não pode estar vazia." }
        return database.withTransaction { insertMessage(conversationId, role, text) }
    }

    private suspend fun insertMessage(conversationId: Uuid, role: Author, text: String): Message {
        val row = MessageEntity(
            conversationId = conversationId.toString(),
            order = messageDao.lastOrder(conversationId.toString()) + 1,
            text = text,
            role = role.name,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insert(row)
        return row.toModel()
    }

    override suspend fun getMessagesPage(conversationId: Uuid, limit: Int, offset: Int): List<Message> {
        require(limit > 0 && offset >= 0)
        return messageDao.getMessagesPage(conversationId.toString(), limit, offset).map { it.toModel() }
    }

    override suspend fun deleteConversation(conversationId: Uuid) =
        conversationDao.deleteById(conversationId.toString())
}

private fun ConversationEntity.toModel() = Conversation(Uuid.parse(id), title, createdAt)

private fun MessageEntity.toModel() = Message(order, Uuid.parse(conversationId), Author.valueOf(role), text, timestamp)
