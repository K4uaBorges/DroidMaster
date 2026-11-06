package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import api.interfaces.ChatAssistant
import errors.GeminiEmptyResponseException
import errors.GeminiHttpException
import errors.MissingApiKeyException
import errors.OfflineException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.Message
import model.Author
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
    private val scope: CoroutineScope,
    private val assistant: ChatAssistant? = null
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
            var savedId: Uuid? = null
            var activeVersion = selectionVersion
            try {
                savedId = repository.appendUserMessage(conversationId, text)
                // A completed save must not take the user back to a chat they have left.
                if (conversationController.selectionVersion == selectionVersion) {
                    conversationController.selectSavedConversation(savedId)
                    activeVersion = conversationController.selectionVersion
                }
                assistant?.let {
                    val history = repository.observeMessages(savedId).first()
                    val answer = it.generate(history)
                    repository.appendMessage(savedId, Author.MODEL, answer)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (conversationController.selectionVersion == activeVersion) {
                    screenState = screenState.copy(
                        messageText = if (savedId == null) screenState.messageText.ifBlank { text } else screenState.messageText,
                        error = if (savedId == null) "Não foi possível guardar a mensagem. Tenta novamente." else errorMessage(error)
                    )
                }
            } finally {
                screenState = screenState.copy(isSending = false)
            }
        }
    }

    private fun errorMessage(error: Exception): String = when (error) {
        is MissingApiKeyException -> "Configura a chave Gemini nas definições para obter uma resposta."
        is OfflineException -> "Sem ligação à Internet. A mensagem ficou guardada no histórico."
        is GeminiEmptyResponseException -> "O Gemini não devolveu texto. Tenta novamente."
        is GeminiHttpException -> when (error.statusCode) {
            400 -> "O Gemini rejeitou o pedido (HTTP 400)."
            401, 403 -> "A chave Gemini não é válida ou não tem acesso ao serviço (HTTP ${error.statusCode})."
            404 -> "O modelo Gemini não está disponível (HTTP 404)."
            429 -> "O limite de pedidos do Gemini foi atingido (HTTP 429)."
            in 500..599 -> "O serviço Gemini está temporariamente indisponível (HTTP ${error.statusCode})."
            else -> "O pedido ao Gemini falhou (HTTP ${error.statusCode})."
        }
        is java.io.IOException -> "Falha de ligação ao Gemini. Verifica a Internet e tenta novamente."
        else -> "Não foi possível obter ou guardar a resposta. Tenta novamente."
    }

    fun retryResponse(): Job? {
        val conversationId = conversationController.currentConversationId.value ?: return null
        val provider = assistant ?: return null
        if (screenState.isSending || screenState.messages.lastOrNull()?.role != Author.USER) return null
        val version = conversationController.selectionVersion
        screenState = screenState.copy(isSending = true, error = null)
        return scope.launch {
            try {
                val history = repository.observeMessages(conversationId).first()
                val answer = provider.generate(history)
                repository.appendMessage(conversationId, Author.MODEL, answer)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (conversationController.selectionVersion == version) {
                    screenState = screenState.copy(error = errorMessage(error))
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
