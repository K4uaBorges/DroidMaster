package controller

import android.content.Context
import android.net.Uri
import controller.shared.createTemporaryMarkdownFile
import controller.shared.saveConversationToUri
import controller.shared.shareMarkdownFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

class SharedController(private val repository: ConversationRepository) {
    suspend fun shareWithPeople(context: Context, conversationId: Uuid) {
        val file = withContext(Dispatchers.IO) {
            createTemporaryMarkdownFile(context, conversationId, repository)
        }
        shareMarkdownFile(context, file)
    }

    suspend fun saveToUri(context: Context, uri: Uri, conversationId: Uuid) {
        withContext(Dispatchers.IO) {
            saveConversationToUri(context, uri, conversationId, repository)
        }
    }
}
