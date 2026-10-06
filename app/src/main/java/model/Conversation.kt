package model

import kotlin.uuid.Uuid

data class Conversation(
    val id: Uuid,
    val title: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = createdAtMillis
) {
    init {
        require(title.isNotBlank()) { "A conversa tem de ter título." }
    }
}