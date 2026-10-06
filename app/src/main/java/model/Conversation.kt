package model

data class Conversation (
    val id: String,
    val title: String,
    val createdAt: String
){
    init {
        require(title.isNotBlank()) { "A conversa tem de ter título." }
    }
}