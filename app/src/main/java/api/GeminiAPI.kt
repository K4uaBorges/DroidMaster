package api

import api.dto.GeminiContent
import api.dto.GeminiPart
import api.dto.GeminiRequest
import api.dto.GeminiResponse
import api.interfaces.AIProvider
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
import io.ktor.client.request.preparePost
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readLineStrict
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private const val DEFAULT_MODEL = "gemini-3.5-flash-lite" //"gemini-3.8-flash"


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

    private fun validateRequest(
        contents: List<GeminiContent>
    ) {
        require(apiKey.isNotBlank()) {
            "A API key não está configurada."
        }

        require(model.matches(Regex("[A-Za-z0-9._-]+"))) {
            "Modelo Gemini inválido."
        }

        require(contents.isNotEmpty()) {
            "A conversa não pode estar vazia."
        }

        require(contents.all { content ->
            content.role in setOf("model", "user") &&
                    content.parts.isNotEmpty() &&
                    content.parts.all { it.text.isNotBlank() }
        }) {
            "As mensagens devem conter texto e um role 'user' ou 'model'."
        }
    }

    /** Receives the history and latest message without keeping shared conversation state. */
    override suspend fun generate(
        contents: List<GeminiContent>,
        systemInstruction: String
    ): String = generateContent(contents, systemInstruction)

    /** Includes the history and latest user message, in chronological order. */
    private suspend fun generateContent(
        contents: List<GeminiContent>,
        systemInstruction: String
    ): String {

        validateRequest(contents)

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
                                text = systemInstruction
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

    /*
    --------------------------------------------------
                STREAM
    --------------------------------------------------
     */

    override fun generateStream(
        contents: List<GeminiContent>,
        systemInstruction: String
    ): Flow<String> = flow {

        validateRequest(contents)

        val json = Json {                   // Ignora propriedades que não são as que queremos.
            ignoreUnknownKeys = true
        }

        var hasText = false
        var finishReason: String? = null

        client.preparePost(
            "https://generativelanguage.googleapis.com/" +
                    "v1beta/models/$model:streamGenerateContent?alt=sse"
        ) {
            header("x-goog-api-key", apiKey)
            header("Accept", "text/event-stream")
            contentType(ContentType.Application.Json)

            setBody(
                GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(
                        parts = listOf(
                            GeminiPart(text = systemInstruction)
                        )
                    )
                )
            )

        }.execute { response ->

            if (!response.status.isSuccess()) {
                throw GeminiHttpException(response.status.value)
            }

            val channel = response.bodyAsChannel()          // Permite ler progressivamente os dados do corpo da resposta HTTP.
            val dataLines = mutableListOf<String>()

            while (true) {

                val line = channel.readLineStrict() ?: break            // Lê uma linha de texto, sem incluir os caracteres de mudança de linha.

                if (line.startsWith("data:")) {
                    dataLines.add(
                        line.removePrefix("data:").removePrefix(" ")
                    )
                }

                if (line.isEmpty() && dataLines.isNotEmpty()) {

                    val data = dataLines.joinToString("\n")
                    dataLines.clear()

                    if (data.isBlank()) continue

                    val result = json.decodeFromString<GeminiResponse>(data)

                    val candidate = result.candidates.firstOrNull()

                    candidate?.finishReason?.let {
                        finishReason = it
                    }

                    val text = candidate?.content?.parts
                        .orEmpty()
                        .filterNot { it.thought }
                        .joinToString("") { it.text }

                    if (text.isNotEmpty()) {

                        if (text.isNotBlank()) {
                            hasText = true
                        }

                        emit(text)
                    }
                }
            }

            check(dataLines.isEmpty()) {
                "A receção do evento SSE foi interrompida."
            }
        }

        if (!hasText) {
            throw GeminiEmptyResponseException()
        }

        check(finishReason == "STOP") {
            "A resposta não terminou normalmente: $finishReason"
        }
    }
}

