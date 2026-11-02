package storage

import android.app.Application
import androidx.room.Room
import androidx.room.withTransaction
import controller.shared.createTemporaryMarkdownFile
import controller.utis.conversationToMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import model.Author
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import storage.entity.MessageEntity
import storage.repository.RoomConversationRepository
import java.io.StringWriter
import java.io.File
import java.util.UUID
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RoomConversationRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomConversationRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        repository = RoomConversationRepository(database)
    }

    @After
    fun tearDown() { database.close() }

    @Test
    fun firstMessageCreatesOneConversationAndContinuationUsesIt() = runBlocking {
        val id = repository.appendUserMessage(null, "  Primeira mensagem  ")
        assertEquals(id, repository.appendUserMessage(id, "Segunda mensagem"))
        val conversations = repository.conversations.first()
        assertEquals(1, conversations.size)
        assertEquals("Primeira mensagem", conversations.single().title)
        val messages = repository.observeMessages(id).first()
        assertEquals(listOf("Primeira mensagem", "Segunda mensagem"), messages.map { it.text })
        assertEquals(listOf(1L, 2L), messages.map { it.order })
        assertTrue(messages.all { it.role == Author.USER && it.conversationId == id })
    }

    @Test
    fun sequenceDeterminesOrderEvenWhenTimestampsAreEqual() = runBlocking {
        val id = repository.appendUserMessage(null, "primeira")
        database.messageDao().insert(MessageEntity(
            conversationId = id.toString(), order = 3, text = "terceira", role = "USER", timestamp = 0
        ))
        database.messageDao().insert(MessageEntity(
            conversationId = id.toString(), order = 2, text = "segunda", role = "MODEL", timestamp = 0
        ))
        assertEquals(listOf(1L, 2L, 3L), repository.observeMessages(id).first().map { it.order })
        val page = repository.getMessagesPage(id, limit = 1, offset = 1)
        assertEquals("segunda", page.single().text)
        assertEquals(Author.MODEL, page.single().role)
        assertTrue(repository.getMessagesPage(Uuid.random(), 100, 0).isEmpty())
    }

    @Test
    fun failedFirstInsertRollsBackTheConversationToo() = runBlocking {
        withContext(Dispatchers.IO) {
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER reject_message BEFORE INSERT ON messages " +
                    "BEGIN SELECT RAISE(ABORT, 'test failure'); END"
            )
        }
        try {
            repository.appendUserMessage(null, "Não deve ficar uma conversa vazia")
            fail("The trigger should reject the message")
        } catch (_: android.database.sqlite.SQLiteException) {
            // The transaction must roll back both the parent and child insert.
        }
        assertTrue(repository.conversations.first().isEmpty())
    }

    @Test
    fun foreignKeyRejectsOrphansAndDeletesMessagesWithTheirConversation() = runBlocking {
        try {
            repository.appendMessage(Uuid.random(), Author.USER, "órfã")
            fail("An unknown conversation must not accept messages")
        } catch (_: android.database.sqlite.SQLiteException) {
            // Foreign keys must be enforced by the actual generated database.
        }
        val id = repository.appendUserMessage(null, "pergunta")
        repository.appendMessage(id, Author.MODEL, "resposta local de teste")
        repository.deleteConversation(id)
        assertNull(repository.getConversation(id))
        assertTrue(repository.observeMessages(id).first().isEmpty())
    }

    @Test
    fun concurrentAppendsAllocateDistinctSequentialOrders() = runBlocking {
        val id = repository.appendUserMessage(null, "início")
        coroutineScope {
            (1..10).map { index ->
                async(Dispatchers.Default) { repository.appendMessage(id, Author.USER, "mensagem $index") }
            }.awaitAll()
        }
        val messages = repository.observeMessages(id).first()
        assertEquals((1L..11L).toList(), messages.map { it.order })
        assertEquals(11, messages.map { it.text }.distinct().size)
    }

    @Test
    fun reopeningTheDatabasePreservesConversationAndMessages() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val name = "persistence-${UUID.randomUUID()}.db"
        var diskDatabase = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            val id = RoomConversationRepository(diskDatabase).appendUserMessage(null, "persistente")
            diskDatabase.close()
            diskDatabase = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val reopened = RoomConversationRepository(diskDatabase)
            assertEquals(id, reopened.conversations.first().single().id)
            assertEquals("persistente", reopened.observeMessages(id).first().single().text)
        } finally {
            diskDatabase.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun markdownExportReadsAllPagesInOrder() = runBlocking {
        val id = repository.appendUserMessage(null, "mensagem 1")
        database.withTransaction {
            for (index in 2..205) {
                database.messageDao().insert(MessageEntity(
                    conversationId = id.toString(), order = index.toLong(),
                    text = "mensagem $index", role = "USER", timestamp = 0
                ))
            }
        }
        val output = StringWriter()
        conversationToMarkdown(id, repository, output)
        val markdown = output.toString()
        assertEquals(205, Regex("(?m)^## Utilizador$").findAll(markdown).count())
        assertTrue(markdown.endsWith("mensagem 205\n\n"))
        assertTrue(markdown.indexOf("mensagem 100\n") < markdown.indexOf("mensagem 101\n"))
    }

    @Test
    fun sharingCreatesMarkdownInTheDesignatedDirectory() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val id = repository.appendUserMessage(null, "Olá\n[link](https://example.org)")
        repository.appendMessage(id, Author.MODEL, "Resposta para partilhar")
        val file = createTemporaryMarkdownFile(context, id, repository)
        try {
            assertEquals(File(context.cacheDir, "shared_conversations").canonicalFile, file.parentFile!!.canonicalFile)
            val markdown = file.readText()
            assertTrue(markdown.contains("Olá\n[link](https://example.org)"))
            assertTrue(markdown.contains("## IA\n\nResposta para partilhar"))
        } finally {
            file.delete()
        }
    }

    @Test
    fun sqlLikeTextAndIdsAreTreatedAsData() = runBlocking {
        val payload = "x'); DROP TABLE messages; --"
        val id = repository.appendUserMessage(null, payload)
        val injectedId = "${id}' OR 1=1 --"
        assertNull(database.conversationDao().getById(injectedId))
        assertTrue(database.messageDao().getByConversationId(injectedId).isEmpty())
        assertEquals(payload, repository.observeMessages(id).first().single().text)
        repository.appendUserMessage(id, "A tabela continua acessível")
        assertEquals(2, repository.observeMessages(id).first().size)
    }
}
