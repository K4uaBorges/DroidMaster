package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.util.date.getTimeMillis
import model.Conversation

data class ConversationViewState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversationId: Long? = null
)

class ConversationController{
    var screenState by mutableStateOf(
        ConversationViewState()
    )
        private set

    private var nextConversationId = 1L

    fun createConversation(firstMessage: String): Long {

        val conversation = Conversation(
            id = nextConversationId,
            title = createTitle(firstMessage),
            createdAt = System.currentTimeMillis()
        )

        nextConversationId++

        screenState = screenState.copy(
            conversations = listOf(conversation) + screenState.conversations,
            currentConversationId = conversation.id
        )

        return conversation.id
    }

    fun selectConversation(conversationId: Long) {

        val conversationExists = screenState.conversations.any{
            it.id == conversationId
        }

        if(!conversationExists) {
            return
        }

        screenState = screenState.copy(
            currentConversationId = conversationId
        )
    }

    fun newConversation() {
        screenState = screenState.copy(
            currentConversationId = null
        )
    }

    private fun createTitle(firstMessage: String): String {

        val text = firstMessage.trim()

        return if (text.length <= 30) {
            text
        } else {
            text.take(30) + "..."
        }
    }
}