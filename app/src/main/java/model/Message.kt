package model

data class Message(
    val id: Long,
    val conversationId: Long,
    val role: Author,
    val text: String,
    val timestamp: Long
){
    init {
        require(text.isNotBlank()) { "A mensagem não pode estar vazia." }
    }
}
