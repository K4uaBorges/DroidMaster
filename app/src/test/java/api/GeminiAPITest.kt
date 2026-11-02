package api

import api.dto.GeminiContent
import api.dto.GeminiPart
import api.dto.GeminiRequest
import errors.GeminiEmptyResponseException
import errors.GeminiHttpException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeminiAPITest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun userContents(text: String) =
        listOf(GeminiContent(parts = listOf(GeminiPart(text)), role = "user"))

    @Test
    fun sendsHistoryAndMentorInstructionAndReadsOnlyAnswer() = runBlocking {
        val history = listOf(
            GeminiContent(listOf(GeminiPart("Como crio uma Activity?")), "user"),
            GeminiContent(listOf(GeminiPart("Usa ComponentActivity.")), "model"),
            GeminiContent(listOf(GeminiPart("E com Compose?")), "user")
        )
        val client = createGeminiClient(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(
                "https://generativelanguage.googleapis.com/v1beta/models/test-model:generateContent",
                request.url.toString()
            )
            assertEquals("test-key", request.headers["x-goog-api-key"])
            assertFalse(request.url.parameters.contains("key"))
            val body = Json.decodeFromString<GeminiRequest>((request.body as TextContent).text)
            assertEquals(history, body.contents)
            assertTrue(body.systemInstruction.parts.single().text.contains("Android"))
            respond(
                """{"candidates":[{"content":{"role":"model","parts":[{"text":"Interno","thought":true},{"text":"Usa "},{"text":"setContent."}]},"finishReason":"STOP"}],"usageMetadata":{"totalTokenCount":12}}""",
                HttpStatusCode.OK,
                jsonHeaders
            )
        })
        try {
            assertEquals("Usa setContent.", GeminiAPI(client, "test-key", "test-model").generate(history))
        } finally {
            client.close()
        }
    }


    @Test
    fun exposesHttpStatusWithoutLeakingResponseBody() = runBlocking {
        for (status in listOf(400, 401, 403, 404, 429, 500, 503)) {
            val client = createGeminiClient(MockEngine {
                respond("private server details", HttpStatusCode.fromValue(status))
            })
            try {
                GeminiAPI(client, "test-key", "test-model").generate(userContents("Olá"))
                fail("Expected HTTP failure")
            } catch (error: GeminiHttpException) {
                assertEquals(status, error.statusCode)
                assertFalse(error.message.orEmpty().contains("private server details"))
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun rejectsBlockedOrEmptyResponses() = runBlocking {
        val client = createGeminiClient(MockEngine {
            respond("""{"promptFeedback":{"blockReason":"SAFETY"}}""", HttpStatusCode.OK, jsonHeaders)
        })
        try {
            GeminiAPI(client, "test-key", "test-model").generate(userContents("Olá"))
            fail("Expected empty response failure")
        } catch (_: GeminiEmptyResponseException) {
            // A successful HTTP status does not guarantee a text answer.
        } finally {
            client.close()
        }
    }

    @Test
    fun waitingRequestSuspendsAndCanBeCancelled() = runBlocking {
        withTimeout(5000) {
            val started = CompletableDeferred<Unit>()
            val releaseResponse = CompletableDeferred<Unit>()
            val client = createGeminiClient(MockEngine {
                started.complete(Unit)
                releaseResponse.await()
                respond("{}", HttpStatusCode.OK, jsonHeaders)
            })
            try {
                val pending = async {
                    GeminiAPI(client, "test-key", "test-model").generate(userContents("Olá"))
                }
                started.await()
                assertFalse(pending.isCompleted)
                pending.cancelAndJoin()
                assertTrue(pending.isCancelled)
            } finally {
                releaseResponse.complete(Unit)
                client.close()
            }
        }
    }
}
