package api

import api.interfaces.ChatAssistant
import model.Author
import model.Message
import kotlin.uuid.Uuid

class ConversationTitleGenerator(
    private val assitant: ChatAssistant
) {

    suspend fun generateTitle(
        conversationId: Uuid,
        firstMessage: String
    ): String {

        val prompt = """
            Gera um título curto e descritivo para a conversa iniciada
             com a mensagem descrita abaixo.
             Regras a cumprir:
             - Utiliza entre 2 a 5 palavras.
             - Identifica o tema principal da mensagem.
             - Utiliza o idioma da mensagem original.
             - Não utilizes aspas nem formatação Markdown.
             - Devolve apenas o título.
             Mensagem:
             $firstMessage
        """.trimIndent()

        val message = Message(
            order = 1L,
            conversationId = conversationId,
            role = Author.USER,
            text = prompt
        )

        return assitant.generate(listOf(message)).trim()
    }
}