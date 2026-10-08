package apps

import android.app.Application
import api.GeminiAPI
import api.createGeminiClient
import controller.ChatController
import controller.ConversationController
import controller.SharedController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import storage.AppDatabase
import storage.repository.RoomConversationRepository

class DroidMasterApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val database by lazy { AppDatabase.getInstance(this) }
    val conversationRepository by lazy { RoomConversationRepository(database) }
    val sharedController by lazy { SharedController(conversationRepository) }

    val geminiClient by lazy { createGeminiClient() }

    fun createGeminiAPI(apiKey: String): GeminiAPI = GeminiAPI(geminiClient, apiKey)

    val conversationController by lazy {
        ConversationController(conversationRepository, applicationScope)
    }

    val chatController by lazy {
        ChatController(
            conversationController,
            conversationRepository,
            applicationScope
        )
    }
}
