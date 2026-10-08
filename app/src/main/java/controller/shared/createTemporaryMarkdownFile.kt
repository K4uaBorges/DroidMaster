package controller.shared

import android.content.Context
import controller.utis.conversationToMarkdown
import storage.repository.ConversationRepository
import java.io.File
import kotlin.uuid.Uuid

suspend fun createTemporaryMarkdownFile(
    context: Context,
    conversationId: Uuid,
    repository: ConversationRepository
): File {

    val directory = File(context.cacheDir, "shared_conversations")
    if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
        throw java.io.IOException("Não foi possível criar a pasta de partilha.")
    }
    val file = File.createTempFile("conversation_${conversationId}_", ".md", directory)

    try {
        file.bufferedWriter().use { writer ->
            conversationToMarkdown(
                conversationId = conversationId,
                repository = repository,
                writer = writer
            )
        }
    } catch (error: Throwable) {
        file.delete()
        throw error
    }

    return file
}
