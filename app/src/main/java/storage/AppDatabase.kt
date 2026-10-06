package storage

import android.content.Context
import androidx.room.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import storage.dao.ConversationDao
import storage.dao.MessageDao
import storage.entity.ConversationEntity
import storage.entity.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "droidmaster.db"
                ).build().also { instance = it }
            }
    }
}