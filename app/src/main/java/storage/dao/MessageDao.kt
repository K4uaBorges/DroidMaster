package storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import model.Message
import storage.entity.MessageEntity
import kotlin.uuid.Uuid

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: MessageEntity): Uuid

    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY timestamp ASC"
    )

    fun observeByConversationId(
        conversationId: Uuid
    ): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages " +
                   "WHERE conversationId = :conversationId " +
                   "ORDER BY timestamp ASC")
    suspend fun getByConversationId(
        conversationId: Uuid
    ): List<MessageEntity>

    @Query("""
    SELECT * FROM messages
    WHERE conversationId = :conversationId
    ORDER BY timestamp ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getMessagesPage(
        conversationId: Uuid,
        limit: Int,
        offset: Int
    ): List<Message>
}