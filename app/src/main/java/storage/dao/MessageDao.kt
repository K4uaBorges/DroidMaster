package storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import storage.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: MessageEntity)

    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY messageOrder ASC"
    )

    fun observeByConversationId(
        conversationId: String
    ): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages " +
                   "WHERE conversationId = :conversationId " +
                   "ORDER BY messageOrder ASC")
    suspend fun getByConversationId(
        conversationId: String
    ): List<MessageEntity>

    @Query("""
    SELECT * FROM messages
    WHERE conversationId = :conversationId
    ORDER BY messageOrder ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun getMessagesPage(
        conversationId: String,
        limit: Int,
        offset: Int
    ): List<MessageEntity>

    @Query("SELECT COALESCE(MAX(messageOrder), 0) FROM messages WHERE conversationId = :conversationId")
    suspend fun lastOrder(conversationId: String): Long
}
