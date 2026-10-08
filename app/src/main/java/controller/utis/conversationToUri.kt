package controller.utis

import android.content.Context
import android.net.Uri
import controller.shared.saveConversationToUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

suspend fun writeConversationToUri(
    context: Context,
    uri: Uri,
    conversationId: Uuid,
    repository: ConversationRepository
) = withContext(Dispatchers.IO) {
    saveConversationToUri(context, uri, conversationId, repository)
}
