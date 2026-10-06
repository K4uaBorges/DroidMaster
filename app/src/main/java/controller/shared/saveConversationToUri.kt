package controller.shared

import android.content.Context
import android.net.Uri
import controller.utis.conversationToMarkdown
import storage.dao.MessageDao
import kotlin.uuid.Uuid

suspend fun saveConversationToUri(
    context: Context,
    uri: Uri,
    conversationId: Uuid,
    messageDao: MessageDao
) {

    context.contentResolver
        .openOutputStream(uri)
        ?.bufferedWriter()
        ?.use { writer ->

            conversationToMarkdown(
                conversationId = conversationId,
                messageDao = messageDao,
                writer = writer
            )
        }
}