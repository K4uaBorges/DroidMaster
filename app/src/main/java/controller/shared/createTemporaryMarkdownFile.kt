package controller.shared

import android.content.Context
import controller.utis.conversationToMarkdown
import storage.dao.MessageDao
import java.io.File
import kotlin.uuid.Uuid

suspend fun createTemporaryMarkdownFile(
    context: Context,
    conversationId: Uuid,
    messageDao: MessageDao
): File {

    val file = File(
        context.cacheDir,
        "shared_conversation.md"
    )

    file.bufferedWriter().use { writer ->

        conversationToMarkdown(
            conversationId = conversationId,
            messageDao = messageDao,
            writer = writer
        )
    }

    return file
}