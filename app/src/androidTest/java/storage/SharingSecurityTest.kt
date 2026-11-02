package storage

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import controller.shared.createMarkdownShareIntent
import controller.shared.createTemporaryMarkdownFile
import kotlinx.coroutines.runBlocking
import model.Author
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import storage.repository.RoomConversationRepository
import java.io.File
import DroidMaster.apps.R
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class SharingSecurityTest {
    @Test
    fun backupRulesExcludeDatabaseAndJournalsForBothTransferTypes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expected = setOf("droidmaster.db", "droidmaster.db-wal", "droidmaster.db-shm")
        fun exclusions(resource: Int): Map<String, Set<String>> {
            val found = mutableMapOf<String, MutableSet<String>>()
            var section = "legacy"
            context.resources.getXml(resource).use { parser ->
                var event = parser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "cloud-backup", "device-transfer" -> section = parser.name
                            "exclude" -> if (parser.getAttributeValue(null, "domain") == "database") {
                                found.getOrPut(section) { mutableSetOf() }
                                    .add(parser.getAttributeValue(null, "path"))
                            }
                        }
                    }
                    event = parser.next()
                }
            }
            return found
        }
        assertEquals(expected, exclusions(R.xml.backup_rules)["legacy"])
        val modern = exclusions(R.xml.data_extraction_rules)
        assertEquals(expected, modern["cloud-backup"])
        assertEquals(expected, modern["device-transfer"])
    }

    @Suppress("DEPRECATION")
    @Test
    fun markdownIsReadableWithReadOnlyGrantAndOtherCacheFilesAreRejected() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = RoomConversationRepository(database)
            val id = repository.appendUserMessage(null, "Olá\n[link](https://example.org)")
            repository.appendMessage(id, Author.MODEL, "Resposta para partilhar")
            val file = createTemporaryMarkdownFile(context, id, repository)
            try {
                val intent = createMarkdownShareIntent(context, file)
                assertEquals(Intent.ACTION_SEND, intent.action)
                assertEquals("text/markdown", intent.type)
                assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
                assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
                assertEquals("content", uri.scheme)
                val markdown = context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
                assertTrue(markdown.contains("Olá\n[link](https://example.org)"))
                assertTrue(markdown.contains("## IA\n\nResposta para partilhar"))
                try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(context.cacheDir, "private.txt"))
                    fail("The provider must reject files outside the designated directory")
                } catch (_: IllegalArgumentException) {
                    // The cache root must not be shareable.
                }
            } finally {
                file.delete()
            }
        } finally {
            database.close()
        }
    }
}
