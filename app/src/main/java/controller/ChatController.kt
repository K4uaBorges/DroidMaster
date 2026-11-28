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
import view.theme.LocalDroidMasterTheme
import kotlin.uuid.Uuid

data class ChatViewState(
    val messageText: String = "",
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
    val streamingMessage: Message? = null
)

class ChatController(
    private val conversationController: ConversationController,
    private val repository: ConversationRepository,
    private val scope: CoroutineScope,
    private val assistant: ChatAssistant? = null
) {
    var screenState by mutableStateOf(ChatViewState())
        private set

    var isTemporaryChat by mutableStateOf(false)
        private set

    private var temporaryConversationId = Uuid.random()     // Estas propriedades servem para identificar se estamos numa conversa temporária,
    private var temporaryVersion = 0L                       //  atribuir-lhe um identificador apenas em memória e controlar os pedidos em curso.
    private var temporaryJob: Job? = null

    init {
        scope.launch {

            conversationController.currentConversationId.collectLatest { conversationId ->

                if (isTemporaryChat) return@collectLatest                   // impede que o observador das conversas guardadas substitua as mensagens temporárias enquanto estivermos nesse modo.

                screenState = screenState.copy(
                    messages = emptyList(),
                    streamingMessage = screenState.streamingMessage?.takeIf {
                        it.conversationId == conversationId
                    })

                if (conversationId != null) {

                    repository.observeMessages(conversationId)
                        .catch { screenState = screenState.copy(error = "Não foi possível carregar as mensagens.") }
                        .collect { messages -> screenState = screenState.copy(messages = messages) }
                }
            }
        }
    }

    private suspend fun collectResponse(
        provider: ChatAssistant,
        history: List<Message>,
        conversationId: Uuid,
        isVisible: () -> Boolean        // Importante para evitar o problema das mensagens aparecerem nas conversas erradas.
    ) : String {

        val response = StringBuilder()

        val order = (history.lastOrNull()?.order ?: 0L) + 1L    // VER O QUE É ISTO
        val timestamp = System.currentTimeMillis()

        provider.generateStream(history).collect { fragment ->

            response.append(fragment)

            if (isVisible() && response.isNotBlank()) {

                screenState = screenState.copy(
                    streamingMessage = Message(
                        order = order,
                        conversationId = conversationId,
                        role = Author.MODEL,
                        text = response.toString(),
                        timestamp = timestamp
                    )
                )
            }
        }

        if (response.isBlank()) {
            throw GeminiEmptyResponseException()
        }

        return response.toString()
    }

    fun changeMessageText(nextText: String) {
        screenState = screenState.copy(messageText = nextText)
    }

    fun sendMessage(): Job? {
        val text = screenState.messageText.trim()

        if (text.isEmpty() || screenState.isSending) return null

        if (isTemporaryChat) return sendTemporaryMessage(text)

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

                    val answer = collectResponse(
                        provider = it,
                        history = history,
                        conversationId = savedId,
                        isVisible = {
                            !isTemporaryChat &&
                                    conversationController.currentConversationId.value == savedId
                        }
                    )

                    repository.appendMessage(savedId, Author.MODEL, answer)
                        // Só chegamos aqui depois da recolha do Flow.
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
                if (!isTemporaryChat) {
                    screenState = screenState.copy(
                        isSending = false,
                        streamingMessage = null
                    )
                }
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
        val version = conversationController.selectionVersion

        if (screenState.isSending || screenState.messages.lastOrNull()?.role != Author.USER) return null

        screenState = screenState.copy(
            isSending = true,
            error = null
        )

        return scope.launch {
            try {
                val history = repository.observeMessages(conversationId).first()

                val answer = collectResponse(
                    provider = provider,
                    history = history,
                    conversationId = conversationId,
                    isVisible = {
                        !isTemporaryChat &&
                                conversationController.currentConversationId.value == conversationId
                    }
                )

                repository.appendMessage(conversationId, Author.MODEL, answer)
                    // Só chegamos aqui depois da recolha do Flow.

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                if (conversationController.selectionVersion == version) {
                    screenState = screenState.copy(error = errorMessage(error))
                }

            } finally {
                if (!isTemporaryChat) {
                    screenState = screenState.copy(
                        isSending = false,
                        streamingMessage = null
                    )
                }
            }
        }
    }

    fun openConversation(conversationId: Uuid) {

        val isCurrent = conversationController.currentConversationId.value == conversationId

        if (conversationController.selectConversation(conversationId)) {

            screenState = screenState.copy(
                messageText = "",
                messages = if (isCurrent) screenState.messages else emptyList(),
                error = null,
                streamingMessage = if (isCurrent) screenState.streamingMessage else null
            )
        }
    }

    fun newConversation() {

        conversationController.newConversation()

        screenState = screenState.copy(
            messageText = "",
            messages = emptyList(),
            error = null,
            streamingMessage = null)
    }


    fun toggleTemporaryMode() {
        // Só permite alterar o modo antes de começar a conversa.

        if (screenState.isSending || screenState.messages.isNotEmpty()) {
            return
        }

        if (!isTemporaryChat) {
            temporaryJob?.cancel()
            temporaryVersion++

            temporaryConversationId = Uuid.random()
            conversationController.newConversation()
        }

        isTemporaryChat = !isTemporaryChat
    }

    fun startTemporaryChat() {
        temporaryJob?.cancel()
        temporaryVersion++

        temporaryConversationId = Uuid.random()
        isTemporaryChat = true

        conversationController.newConversation()
        screenState = ChatViewState()
    }

    fun exitTemporaryChat() {
        temporaryJob?.cancel()
        temporaryVersion++

        isTemporaryChat = false

        conversationController.newConversation()
        screenState = ChatViewState()
    }

    private fun sendTemporaryMessage(text: String) : Job? {

        val provider = assistant ?: return null
        val version = temporaryVersion
        val conversationId = temporaryConversationId

        val userMessage = Message(
            order = screenState.messages.size.toLong() + 1,
            conversationId = conversationId,
            role = Author.USER,
            text = text
        )

        val history = screenState.messages + userMessage
            // Cria uma lista contendo as mensagens anteriores e a nova pergunta.

        screenState = screenState.copy(
            messageText = "",
            messages = history,
            isSending = true,
            error = null
        )

        val job = scope.launch {
            try {
                val answer = collectResponse(
                    provider = provider,
                    history = history,
                    conversationId = conversationId,
                    isVisible = {
                        isTemporaryChat && temporaryVersion == version
                    }
                )

                if (isTemporaryChat && temporaryVersion == version) {
                    // Verifica se ainda estamos na mesma sessão temporária.

                    val modelMessage = Message(
                        order = history.size.toLong() + 1,
                        conversationId = conversationId,
                        role = Author.MODEL,
                        text = answer
                    )

                    screenState = screenState.copy(
                        messages = screenState.messages + modelMessage
                    )
                }
            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                if (isTemporaryChat && temporaryVersion == version) {
                    screenState = screenState.copy(
                        error = errorMessage(error)
                    )
                }

            } finally {
                if (isTemporaryChat && temporaryVersion == version) {
                    screenState = screenState.copy(
                        isSending = false,
                        streamingMessage = null)
                }
            }
        }

        temporaryJob = job
            // Guarda a referência à operação em curso, para podermos cancelá-la quando saímos da conversa temporária.

        return job
    }
}
