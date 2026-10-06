package controller

import android.content.Context
import controller.shared.createTemporaryMarkdownFile
import controller.shared.shareMarkdownFile
import storage.dao.MessageDao
import kotlin.uuid.Uuid

suspend fun shareWithPeople(
    context: Context,
    conversationId: Uuid,
    messageDao: MessageDao
) {

    val file = createTemporaryMarkdownFile(
        context = context,
        conversationId = conversationId,
        messageDao = messageDao
    )

    shareMarkdownFile(
        context = context,
        file = file
    )
}