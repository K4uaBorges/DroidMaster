package controller.utis

import android.content.Context
import android.net.Uri
import model.Author
import storage.dao.MessageDao
import kotlin.uuid.Uuid


suspend fun writeConversationToUri(
    context: Context,
    uri: Uri,
    conversationId: Uuid,
    messageDao: MessageDao
) {
    context.contentResolver
        .openOutputStream(uri)
        ?.bufferedWriter()
        ?.use { writer ->

            val pageSize = 100
            var offset = 0

            while (true) {

                val messages = messageDao.getMessagesPage(
                    conversationId = conversationId,
                    limit = pageSize,
                    offset = offset
                )

                if (messages.isEmpty()) {
                    break
                }

                messages.forEach { message ->

                    when (message.role) {
                        Author.USER ->
                            writer.write("## Utilizador")

                        Author.MODEL ->
                            writer.write("## IA")
                    }

                    writer.newLine()
                    writer.newLine()

                    writer.write(message.text)

                    writer.newLine()
                    writer.newLine()
                }

                offset += pageSize
            }
        }
}