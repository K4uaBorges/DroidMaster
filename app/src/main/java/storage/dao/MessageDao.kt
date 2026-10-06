package storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import storage.entity.MessageEntity
import kotlin.uuid.Uuid

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: MessageEntity): Uuid

    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY createdAtMillis ASC"
    )

    fun observeByConversationId(
        conversationId: Uuid
    ): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages " +
                   "WHERE conversationId = :conversationId " +
                   "ORDER BY createdAtMillis ASC")
    suspend fun getByConversationId(
        conversationId: Uuid
    ): List<MessageEntity>
}