package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import model.Message
import model.MessageRole

data class ChatViewState(
    val messageText: String = "",
    val messages: List<Message> = emptyList()
)

class ChatController {

    var screenState by mutableStateOf(ChatViewState())
        private set

    private val conversationId = 1L

    fun changeMessageText(nextText: String) {
        screenState = screenState.copy(
            messageText = nextText
        )
    }

    fun sendMessage() {
        val textToSend = screenState.messageText.trim()

        if (textToSend.isEmpty()) {
            return
        }

        val messageNumber = screenState.messages.count {
            it.role == MessageRole.USER
        } + 1

        val nextId = screenState.messages.size.toLong() + 1

        val userMessage = Message(
            id = nextId,
            conversationId = conversationId,
            role = MessageRole.USER,
            content = textToSend,
            timestamp = System.currentTimeMillis()
        )

        val droidMasterMessage = Message(
            id = nextId + 1,
            conversationId = conversationId,
            role = MessageRole.MODEL,
            content = "Mensagem $messageNumber recebida",
            timestamp = System.currentTimeMillis()
        )

        screenState = screenState.copy(
            messageText = "",
            messages = screenState.messages + listOf(
                userMessage,
                droidMasterMessage
            )
        )
    }
}