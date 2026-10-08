package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.util.date.getTimeMillis
import model.Conversation
import kotlin.uuid.Uuid

data class ConversationViewState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversationId: Uuid
)

class ConversationController{
    var screenState by mutableStateOf(
        ConversationViewState(currentConversationId = Uuid.random())
    )
        private set

    fun createConversation(firstMessage: String): Uuid {

        val nextConversationId = Uuid.random()

        val conversation = Conversation(
            id = nextConversationId,
            title = createTitle(firstMessage),
            createdAt = System.currentTimeMillis()
        )

        screenState = screenState.copy(
            conversations = listOf(conversation) + screenState.conversations,
            currentConversationId = conversation.id
        )

        return conversation.id
    }

    fun selectConversation(conversationId: Uuid) {

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
            currentConversationId = Uuid.random()
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