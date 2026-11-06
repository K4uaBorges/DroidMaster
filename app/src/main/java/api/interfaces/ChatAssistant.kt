package api.interfaces

import model.Message

fun interface ChatAssistant {
    suspend fun generate(messages: List<Message>): String
}