package model

import kotlin.uuid.Uuid

data class Message(
    val order: Long,
    val conversationId: Uuid,
    val role: Author,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
){
    init {
        require(text.isNotBlank()) { "A mensagem não pode estar vazia." }
    }
}
