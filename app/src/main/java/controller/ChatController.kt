package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import apps.DroidMasterApp
import model.Author
import model.Message
import kotlin.uuid.Uuid

data class ChatViewState(
    val messageText: String = "",
    val messages: List<Message> = emptyList()
)

class ChatController(
    private val conversationController: ConversationController
) {

    var screenState by mutableStateOf(ChatViewState())
        private set

    private val messagesByConversation = mutableMapOf<Uuid, List<Message>>()

    private val conversationId = Uuid.random()

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

        val conversationId = getOrCreateConversation(textToSend)

        val currentMessages = messagesByConversation[conversationId] ?: emptyList()

        val messageNumber = currentMessages.count{
            it.role == Author.USER
        } + 1

        val nextId = screenState.messages.size.toLong() + 1

        val userMessage = Message(
            order = nextId,
            id = nextMessageId++,
            conversationId = conversationId,
            role = Author.USER,
            text = textToSend,
            timestamp = System.currentTimeMillis()
        )

        val droidMasterMessage = Message(
            order = nextId + 1,
            id = nextMessageId++,
            conversationId = conversationId,
            role = Author.MODEL,
            text = "Mensagem $messageNumber recebida",
            timestamp = System.currentTimeMillis()
        )

        val nextMessages = currentMessages + listOf(
            userMessage,
            droidMasterMessage
        )

        messagesByConversation[conversationId] = nextMessages

        screenState = screenState.copy(
            messageText = "",
            messages = nextMessages
        )
    }

    fun openConversation(conversationId: Long) {
        conversationController.selectConversation(
            conversationId
        )

        screenState = ChatViewState(
            messages = messagesByConversation[conversationId] ?: emptyList()
        )
    }

    fun newConversation() {
        conversationController.newConversation()

        screenState = ChatViewState()
    }

    private fun getOrCreateConversation(
        firstMessage: String
    ): Long {

        val currentConversationId =
            conversationController
                .screenState
                .currentConversationId

        if (currentConversationId != null) {
            return currentConversationId
        }

        return conversationController
            .createConversation(firstMessage)
    }
}