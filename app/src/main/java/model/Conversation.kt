package model

import kotlin.uuid.Uuid

data class Conversation (
    val id: Uuid,
    val title: String,
    val createdAt: Long
){
    init {
        require(title.isNotBlank()) { "A conversa tem de ter título." }
    }
}