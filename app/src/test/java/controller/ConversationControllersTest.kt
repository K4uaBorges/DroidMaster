package controller

import kotlinx.coroutines.CompletableDeferred
import api.interfaces.ChatAssistant
import errors.GeminiHttpException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import model.Author
import model.Conversation
import model.Message
import org.junit.Assert.*
import org.junit.Test
import storage.repository.ConversationRepository
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationControllersTest {
    @Test
    fun assistantReceivesConversationHistoryAndResponseIsPersisted() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        val requests = mutableListOf<List<Message>>()
        val chat = ChatController(conversations, repository, backgroundScope, ChatAssistant { messages ->
            requests.add(messages)
            "resposta"
        })
        runCurrent()
        chat.changeMessageText("primeira")
        chat.sendMessage()!!.join()
        chat.changeMessageText("segunda")
        chat.sendMessage()!!.join()
        runCurrent()
        assertEquals(listOf("primeira", "resposta", "segunda"), requests.last().map { it.text })
        assertEquals(listOf(Author.USER, Author.MODEL, Author.USER, Author.MODEL), chat.screenState.messages.map { it.role })
    }

    @Test
    fun apiErrorKeepsUserMessageAndReportsStatus() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope, ChatAssistant { throw GeminiHttpException(429) })
        runCurrent()
        chat.changeMessageText("pergunta")
        chat.sendMessage()!!.join()
        runCurrent()
        assertEquals("pergunta", chat.screenState.messages.single().text)
        assertTrue(chat.screenState.error!!.contains("429"))
        assertFalse(chat.screenState.isSending)
    }

    @Test
    fun retryUsesSavedMessageWithoutDuplicatingIt() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        var attempts = 0
        val chat = ChatController(conversations, repository, backgroundScope, ChatAssistant {
            if (attempts++ == 0) throw GeminiHttpException(429)
            "resposta"
        })
        runCurrent()
        chat.changeMessageText("pergunta")
        chat.sendMessage()!!.join()
        runCurrent()
        chat.retryResponse()!!.join()
        runCurrent()
        assertEquals(listOf("pergunta", "resposta"), chat.screenState.messages.map { it.text })
        assertNull(chat.screenState.error)
    }

    @Test
    fun responseAfterNavigationIsSavedInOriginalConversation() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        val answer = CompletableDeferred<String>()
        val chat = ChatController(conversations, repository, backgroundScope, ChatAssistant { answer.await() })
        runCurrent()
        chat.changeMessageText("pergunta")
        val sending = chat.sendMessage()!!
        runCurrent()
        val originalId = conversations.screenState.currentConversationId!!
        chat.newConversation()
        answer.complete("resposta")
        sending.join()
        runCurrent()
        assertNull(conversations.screenState.currentConversationId)
        assertTrue(chat.screenState.messages.isEmpty())
        assertEquals(listOf("pergunta", "resposta"), repository.getMessagesPage(originalId, 10, 0).map { it.text })
    }

    @Test
    fun firstSendCreatesConversationAndEmptyDraftDoesNot() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope)
        runCurrent()
        assertNull(conversations.screenState.currentConversationId)
        chat.changeMessageText("   ")
        assertNull(chat.sendMessage())
        assertTrue(repository.conversations.value.isEmpty())
        chat.changeMessageText("  Olá  ")
        chat.sendMessage()!!.join()
        runCurrent()
        assertEquals(1, conversations.screenState.conversations.size)
        assertNotNull(conversations.screenState.currentConversationId)
        assertEquals("Olá", chat.screenState.messages.single().text)
        assertEquals("", chat.screenState.messageText)
    }

    @Test
    fun continuationAndNewConversationStayIsolatedAndSameChatCanBeReopened() = runTest {
        val repository = FakeConversationRepository()
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope)
        runCurrent()
        chat.changeMessageText("primeira")
        chat.sendMessage()!!.join()
        runCurrent()
        val firstId = conversations.screenState.currentConversationId!!
        chat.changeMessageText("continuação")
        chat.sendMessage()!!.join()
        runCurrent()
        chat.newConversation()
        runCurrent()
        assertNull(conversations.screenState.currentConversationId)
        assertEquals(1, repository.conversations.value.size)
        chat.changeMessageText("outra conversa")
        chat.sendMessage()!!.join()
        runCurrent()
        assertNotEquals(firstId, conversations.screenState.currentConversationId)
        assertEquals(listOf("outra conversa"), chat.screenState.messages.map { it.text })
        chat.openConversation(firstId)
        runCurrent()
        assertEquals(listOf("primeira", "continuação"), chat.screenState.messages.map { it.text })
        chat.openConversation(firstId)
        runCurrent()
        assertEquals(2, chat.screenState.messages.size)
    }

    @Test
    fun doubleTapDoesNotDuplicateTheFirstSave() = runTest {
        val repository = FakeConversationRepository()
        repository.writeGate = CompletableDeferred()
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope)
        runCurrent()
        chat.changeMessageText("uma vez")
        val saving = chat.sendMessage()!!
        chat.changeMessageText("uma vez")
        assertNull(chat.sendMessage())
        repository.writeGate!!.complete(Unit)
        saving.join()
        runCurrent()
        assertEquals(1, repository.conversations.value.size)
        assertEquals(1, chat.screenState.messages.size)
        assertFalse(chat.screenState.isSending)
    }

    @Test
    fun navigationDuringSaveDoesNotSelectTheOldConversationAgain() = runTest {
        val repository = FakeConversationRepository()
        val firstId = repository.appendUserMessage(null, "existente")
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope)
        runCurrent()
        chat.openConversation(firstId)
        runCurrent()
        repository.writeGate = CompletableDeferred()
        chat.changeMessageText("gravação pendente")
        val saving = chat.sendMessage()!!
        runCurrent()
        chat.newConversation()
        repository.writeGate!!.complete(Unit)
        saving.join()
        runCurrent()
        assertNull(conversations.screenState.currentConversationId)
        assertTrue(chat.screenState.messages.isEmpty())
        assertEquals(2, repository.getMessagesPage(firstId, 100, 0).size)
    }

    @Test
    fun saveFailurePreservesDraftAndDoesNotCreateConversation() = runTest {
        val repository = FakeConversationRepository()
        repository.failWrites = true
        val conversations = ConversationController(repository, backgroundScope)
        val chat = ChatController(conversations, repository, backgroundScope)
        runCurrent()
        chat.changeMessageText("não perder")
        chat.sendMessage()!!.join()
        runCurrent()
        assertEquals("não perder", chat.screenState.messageText)
        assertNotNull(chat.screenState.error)
        assertFalse(chat.screenState.isSending)
        assertNull(conversations.screenState.currentConversationId)
        assertTrue(repository.conversations.value.isEmpty())
    }
}

private class FakeConversationRepository : ConversationRepository {
    override val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val messages = mutableMapOf<Uuid, MutableStateFlow<List<Message>>>()
    var failWrites = false
    var writeGate: CompletableDeferred<Unit>? = null

    override fun observeMessages(conversationId: Uuid) =
        messages.getOrPut(conversationId) { MutableStateFlow(emptyList()) }

    override suspend fun getConversation(conversationId: Uuid) =
        conversations.value.find { it.id == conversationId }

    override suspend fun appendUserMessage(conversationId: Uuid?, text: String): Uuid {
        writeGate?.await()
        if (failWrites) throw java.io.IOException("Test write failure")
        val id = conversationId ?: Uuid.random().also { newId ->
            conversations.value = conversations.value + Conversation(newId, text, 0)
        }
        appendMessage(id, Author.USER, text)
        return id
    }

    override suspend fun appendMessage(conversationId: Uuid, role: Author, text: String): Message {
        val flow = observeMessages(conversationId)
        val message = Message(flow.value.size + 1L, conversationId, role, text, 0)
        flow.value = flow.value + message
        return message
    }

    override suspend fun getMessagesPage(conversationId: Uuid, limit: Int, offset: Int) =
        observeMessages(conversationId).value.drop(offset).take(limit)

    override suspend fun deleteConversation(conversationId: Uuid) {
        conversations.value = conversations.value.filterNot { it.id == conversationId }
        observeMessages(conversationId).value = emptyList()
    }
}
