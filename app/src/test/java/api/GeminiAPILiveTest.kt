package api

import api.dto.GeminiContent
import api.dto.GeminiPart
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.util.UUID

/** Opt in with -PgeminiLiveTests=true. Credentials are read locally, never printed. */
class GeminiAPILiveTest {
    private fun userContents(text: String) =
        listOf(GeminiContent(parts = listOf(GeminiPart(text)), role = "user"))

    @Test
    fun continuesConversationUsingPreviousAndCurrentUserMessages() = runBlocking {
        withObservedApi { observedApi, statuses ->
            val activityName = "Study" + UUID.randomUUID().toString().replace("-", "").take(10)
            val initialTasks = (3..8).random()
            val addedTasks = (2..4).random()
            val firstUserMessage = GeminiContent(listOf(GeminiPart(
                "Estou a criar uma app Android com Jetpack Compose. A minha Activity chama-se " +
                    "$activityName e mostra $initialTasks tarefas. Confirma numa frase curta " +
                    "o nome da Activity e a quantidade, usando algarismos."
            )), "user")

            val firstAnswer = observedApi.generate(listOf(firstUserMessage))
            assertTrue("The first answer must acknowledge the Activity", firstAnswer.contains(activityName))
            assertTrue(
                "The first answer must acknowledge the initial number of tasks",
                Regex("\\b$initialTasks\\b").containsMatchIn(firstAnswer)
            )

            // The second message deliberately omits the original name and quantity.
            // Only the supplied history allows the model to recover both facts.
            val secondUserMessage = GeminiContent(listOf(GeminiPart(
                "Na mesma Activity, acrescenta mais $addedTasks tarefas às que já existiam. " +
                    "Qual é o nome da Activity e o novo total? Responde apenas no formato " +
                    "nomeDaActivity|total, usando algarismos e sem Markdown."
            )), "user")
            val secondAnswer = observedApi.generate(listOf(
                firstUserMessage,
                GeminiContent(listOf(GeminiPart(firstAnswer)), "model"),
                secondUserMessage
            ))

            assertEquals(
                "The answer must combine the previous context with the new user instruction",
                "$activityName|${initialTasks + addedTasks}",
                secondAnswer.trim()
            )
            assertEquals("Both conversation turns must receive an HTTP response", 2, statuses.size)
            assertTrue("Both turns must succeed with HTTP 200 or 201", statuses.all { it == 200 || it == 201 })
            println("Context validation passed: HTTP $statuses; previous Activity and updated task total confirmed.")
        }
    }

    @Test
    fun receivesHttp200Or201AndDeserializesAnswer() = runBlocking {
        withObservedApi { observedApi, statuses ->
            val answer = observedApi.generate(userContents(
                "Para confirmar a comunicação desta app Android, responde apenas DROIDMASTER_OK, " +
                    "sem pontuação, explicações ou Markdown."
            ))

            assertEquals("Exactly one HTTP response must be received", 1, statuses.size)
            assertTrue("Expected HTTP 200 or 201", statuses.single() == 200 || statuses.single() == 201)
            assertEquals("The application must deserialize the model's answer", "DROIDMASTER_OK", answer.trim())
            println("Response validation passed: HTTP ${statuses.single()}; answer received and deserialized.")
        }
    }

    /** Uses the real API and production JSON configuration, with an integration-test timeout. */
    private suspend fun withObservedApi(block: suspend (GeminiAPI, List<Int>) -> Unit) {
        val statuses = mutableListOf<Int>()
        val engine = Java.create()
        val observedClient = createGeminiClient(engine) {
            install(HttpTimeout) {
                requestTimeoutMillis = 180000
                socketTimeoutMillis = 180000
            }
            HttpResponseValidator {
                validateResponse { response ->
                    statuses.add(response.status.value)
                    println("Gemini live response: HTTP ${response.status.value}; endpoint=${response.call.request.url.encodedPath}")
                }
            }
        }
        try {
            block(createApi(observedClient), statuses)
        } finally {
            observedClient.close()
            engine.close()
        }
    }

    @Test
    fun answersAnAndroidQuestion() = runBlocking {
        val answer = api.generate(userContents(
            "Em uma frase curta, que classe base Android devo usar para uma Activity com Compose?"
        ))
        assertTrue("Expected a relevant Android answer", answer.contains("Activity", ignoreCase = true))
    }

    @Test
    fun remembersContextSuppliedByTheApplication() = runBlocking {
        val code = "DROID-" + UUID.randomUUID().toString().take(8)
        val first = GeminiContent(
            listOf(GeminiPart("O código desta sessão de estudo Android é $code. Responde apenas OK.")),
            "user"
        )
        val firstAnswer = api.generate(listOf(first))
        val answer = api.generate(listOf(
            first,
            GeminiContent(listOf(GeminiPart(firstAnswer)), "model"),
            GeminiContent(listOf(GeminiPart("Qual é o código desta sessão? Responde apenas com o código.")), "user")
        ))
        assertTrue("Expected the code from the supplied conversation history", answer.contains(code))
    }

    companion object {
        private lateinit var client: HttpClient
        private lateinit var api: GeminiAPI
        private lateinit var apiKey: String
        private lateinit var model: String

        private fun createApi(httpClient: HttpClient): GeminiAPI =
            if (model.isBlank()) GeminiAPI(httpClient, apiKey)
            else GeminiAPI(httpClient, apiKey, model)

        @BeforeClass
        @JvmStatic
        fun connect() = runBlocking {
            assumeTrue("Live Gemini tests are opt-in", java.lang.Boolean.getBoolean("gemini.liveTests"))
            val envFile = File(checkNotNull(System.getProperty("gemini.envFile")) {
                "Run the live tests through the Gradle test task."
            })
            check(envFile.isFile) { "DroidMaster.env was not found." }
            val variables = envFile.readLines().map { it.trim().removePrefix("export ") }
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .mapNotNull { line ->
                    Regex("^([A-Za-z_][A-Za-z0-9_]*)\\s*[:=]\\s*(.*)$")
                        .matchEntire(line)?.destructured?.let { (name, value) ->
                            name to value.trim().removeSurrounding("\"").removeSurrounding("'")
                        }
                }.toMap()
            val loadedKey = variables["GEMINI_KEY"]
            check(!loadedKey.isNullOrBlank()) { "GEMINI_KEY is missing from DroidMaster.env." }
            apiKey = loadedKey
            client = createGeminiClient(Java.create())
            model = System.getProperty("gemini.model").orEmpty()
            api = createApi(client)
            println("Gemini live tests: model=${model.ifBlank { "API default" }}")
        }

        @AfterClass
        @JvmStatic
        fun disconnect() {
            if (::client.isInitialized) {
                client.close()
                client.engine.close()
            }
        }
    }
}
