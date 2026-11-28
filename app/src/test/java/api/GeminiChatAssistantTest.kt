package api

import api.interfaces.ChatAssistant
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatAssistantTest {

    @Test
    fun defaultStreamEmitsCompleteResponse() = runBlocking {

        val assistant = ChatAssistant {         // Chat assistant simulado.
            "Resposta de teste."
        }

        val fragments = assistant
            .generateStream(emptyList())
            .toList()

        assertEquals(
            listOf("Resposta de teste."),
            fragments
        )
    }
}