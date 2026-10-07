package model

data class Conversation (
    val id: Long,
    val title: String,
    val createdAt: Long
){
    init {
        require(title.isNotBlank()) { "A conversa tem de ter título." }
    }
}