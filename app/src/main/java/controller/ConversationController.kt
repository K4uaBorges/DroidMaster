package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import model.Conversation
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

data class ConversationViewState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversationId: Uuid? = null,
    val error: String? = null
)

class ConversationController(
    private val repository: ConversationRepository,
    scope: CoroutineScope
) {
    var screenState by mutableStateOf(ConversationViewState())
        private set

    private val selectedConversation = MutableStateFlow<Uuid?>(null)
    val currentConversationId = selectedConversation.asStateFlow()
    var selectionVersion: Long = 0
        private set

    init {
        scope.launch {
            repository.conversations
                .catch { screenState = screenState.copy(error = "Não foi possível carregar as conversas.") }
                .collect { conversations ->
                    val previousConversations = screenState.conversations
                    screenState = screenState.copy(conversations = conversations, error = null)
                    val selectedId = selectedConversation.value
                    if (selectedId != null && previousConversations.any { it.id == selectedId } &&
                        conversations.none { it.id == selectedId }) {
                        newConversation()
                    }
                }
        }
    }

    fun selectConversation(conversationId: Uuid): Boolean {
        if (screenState.conversations.none { it.id == conversationId }) return false
        selectSavedConversation(conversationId)
        return true
    }

    internal fun selectSavedConversation(conversationId: Uuid) {
        selectionVersion++
        screenState = screenState.copy(currentConversationId = conversationId)
        selectedConversation.value = conversationId
    }

    fun newConversation() {
        selectionVersion++
        screenState = screenState.copy(currentConversationId = null)
        selectedConversation.value = null
    }
}
