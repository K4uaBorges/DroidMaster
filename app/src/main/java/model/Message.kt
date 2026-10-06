package model

import kotlin.uuid.Uuid

data class Message(
    val id: Uuid,
    val conversationId: Uuid,
    val text: String,
    val author: Author,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    init {
        require(text.isNotBlank()) { "A mensagem não pode estar vazia." }
    }
}
