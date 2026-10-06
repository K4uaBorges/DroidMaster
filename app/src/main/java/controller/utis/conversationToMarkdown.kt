package controller.utis

import model.Author
import storage.dao.MessageDao
import java.io.Writer
import kotlin.uuid.Uuid

suspend fun conversationToMarkdown(
    conversationId: Uuid,
    messageDao: MessageDao,
    writer: Writer
) {
    val pageSize = 100
    var offset = 0

    while (true) {

        val messages = messageDao.getMessagesPage(
            conversationId = conversationId,
            limit = pageSize,
            offset = offset
        )

        if (messages.isEmpty()) {
            return
        }

        messages.forEach { message ->

            val author = when (message.role) {
                Author.USER -> "Utilizador"
                Author.MODEL -> "IA"
            }

            writer.write("## $author\n\n")
            writer.write(message.text)
            writer.write("\n\n")
        }

        offset += pageSize
    }
}