package controller.shared

import android.content.Context
import android.net.Uri
import controller.utis.conversationToMarkdown
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

suspend fun saveConversationToUri(
    context: Context,
    uri: Uri,
    conversationId: Uuid,
    repository: ConversationRepository
) {

    val output = context.contentResolver.openOutputStream(uri)
        ?: throw java.io.IOException("Não foi possível abrir o ficheiro de destino.")
    output.bufferedWriter().use { writer ->
        conversationToMarkdown(
            conversationId = conversationId,
            repository = repository,
            writer = writer
        )
    }
}
