package model
data class Message(
    val id: Long,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val timestamp: Long
)