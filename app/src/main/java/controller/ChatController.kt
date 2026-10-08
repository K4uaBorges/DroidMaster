package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.Message
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

data class ChatViewState(
    val messageText: String = "",
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatController(
    private val conversationController: ConversationController,
    private val repository: ConversationRepository,
    private val scope: CoroutineScope
) {
    var screenState by mutableStateOf(ChatViewState())
        private set

    init {
        scope.launch {
            conversationController.currentConversationId.collectLatest { conversationId ->
                screenState = screenState.copy(messages = emptyList())
                if (conversationId != null) {
                    repository.observeMessages(conversationId)
                        .catch { screenState = screenState.copy(error = "Não foi possível carregar as mensagens.") }
                        .collect { messages -> screenState = screenState.copy(messages = messages) }
                }
            }
        }
    }

    fun changeMessageText(nextText: String) {
        screenState = screenState.copy(messageText = nextText)
    }

    fun sendMessage(): Job? {
        val text = screenState.messageText.trim()
        if (text.isEmpty() || screenState.isSending) return null
        val conversationId = conversationController.currentConversationId.value
        val selectionVersion = conversationController.selectionVersion
        screenState = screenState.copy(messageText = "", isSending = true, error = null)
        return scope.launch {
            try {
                val savedId = repository.appendUserMessage(conversationId, text)
                // A completed save must not take the user back to a chat they have left.
                if (conversationController.selectionVersion == selectionVersion) {
                    conversationController.selectSavedConversation(savedId)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (conversationController.selectionVersion == selectionVersion) {
                    screenState = screenState.copy(
                        messageText = screenState.messageText.ifBlank { text },
                        error = "Não foi possível guardar a mensagem. Tenta novamente."
                    )
                }
            } finally {
                screenState = screenState.copy(isSending = false)
            }
        }
    }

    fun openConversation(conversationId: Uuid) {
        val isCurrent = conversationController.currentConversationId.value == conversationId
        if (conversationController.selectConversation(conversationId)) {
            screenState = screenState.copy(
                messageText = "", messages = if (isCurrent) screenState.messages else emptyList(), error = null
            )
        }
    }

    fun newConversation() {
        conversationController.newConversation()
        screenState = screenState.copy(messageText = "", messages = emptyList(), error = null)
    }
}
