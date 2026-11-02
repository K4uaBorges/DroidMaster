package api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** One reusable client owned by the application; close it when no longer needed. */
fun createGeminiClient(): HttpClient = HttpClient(Android) { configureGemini() }

/** The caller owns the supplied engine (used by tests). */
internal fun createGeminiClient(
    engine: HttpClientEngine,
    configure: HttpClientConfig<*>.() -> Unit = {}
): HttpClient = HttpClient(engine) {
    configureGemini()
    configure()
}

private fun HttpClientConfig<*>.configureGemini() {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 60_000
    }
}
