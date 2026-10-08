package storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import storage.entity.ConversationEntity

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY createdAt DESC, id ASC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)
}
