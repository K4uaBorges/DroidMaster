package api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import api.dto.GeminiContent
import api.dto.GeminiPart
import api.interfaces.ChatAssistant
import errors.MissingApiKeyException
import errors.OfflineException
import model.Author
import model.Message
import storage.GeminiKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll

class GeminiChatAssistant(
    private val context: Context,
    private val keyStore: GeminiKeyStore,
    private val createApi: (String) -> GeminiAPI,
    private val systemInstruction: String = SystemInstructions.ANDROID_MENTOR
) : ChatAssistant {

    override suspend fun generate(
        messages: List<Message>
    ): String {

        val api = prepareApi()

        return api.generate(
            contents = toGeminiContents(messages),
            systemInstruction = systemInstruction
        )
    }

    override fun generateStream(
        messages: List<Message>
    ): Flow<String> = flow {

        val api = prepareApi()

        emitAll(
            api.generateStream(
                contents = toGeminiContents(messages),
                systemInstruction = systemInstruction
            )
        )
    }

    private suspend fun prepareApi(): GeminiAPI {
        val key = keyStore.read()?.takeIf { it.isNotBlank() } ?: throw MissingApiKeyException()
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val capabilities = connectivity.getNetworkCapabilities(network)

        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true ||
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) throw OfflineException()

        return createApi(key)
    }

    private fun toGeminiContents(
        messages: List<Message>
    ) : List<GeminiContent> = messages.map {
        GeminiContent(
            parts = listOf(GeminiPart(text = it.text)),
            role = if (it.role == Author.USER) "user" else "model"
        )
    }
}
