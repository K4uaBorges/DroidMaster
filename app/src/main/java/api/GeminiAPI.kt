package api

import api.dto.GeminiContent
import api.dto.GeminiPart
import api.dto.GeminiRequest
import api.dto.GeminiResponse
import errors.GeminiEmptyResponseException
import errors.GeminiHttpException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

private const val DEFAULT_MODEL = "gemini-3.8-flash"


/** REST API configured with a key. The caller supplies each conversation's context. */
class GeminiAPI(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL
) : AIProvider {

    //override suspend fun generate(prompt: String): String {
    //require(prompt.isNotBlank()) { "A mensagem não pode estar vazia." }
//      return generateContent(
//        listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user"))
//    )
//}


    /** Receives the history and latest message without keeping shared conversation state. */
    override suspend fun generate(contents: List<GeminiContent>): String = generateContent(contents)

    /** Includes the history and latest user message, in chronological order. */
    private suspend fun generateContent(
        contents: List<GeminiContent>
    ): String {
        require(apiKey.isNotBlank()) { "A API Key não está configurada." }
        require(model.matches(Regex("[A-Za-z0-9._-]+"))) { "Modelo Gemini inválido." }
        require(contents.isNotEmpty()) { "A conversa não pode estar vazia." }
        require(contents.all { content ->
            content.role in setOf("user", "model") &&
                    content.parts.isNotEmpty() && content.parts.all { it.text.isNotBlank() }
        }) { "As mensagens devem conter texto e um role user ou model." }

        val response = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        ) {
            header("x-goog-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = "És o DroidMaster, um mentor engenheiro senior, especializado em desenvolvimento Android. " +
                                        "Ajuda o utilizador com explicações claras e exemplos em Kotlin."
                            )
                        )
                    )
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw GeminiHttpException(response.status.value)
        }

        val result = response.body<GeminiResponse>()
        val text = result.candidates.firstOrNull()?.content?.parts.orEmpty()
            .filterNot { it.thought }
            .joinToString("") { it.text }

        if (text.isBlank()) {
            throw GeminiEmptyResponseException()
        }
        return text
    }
}

