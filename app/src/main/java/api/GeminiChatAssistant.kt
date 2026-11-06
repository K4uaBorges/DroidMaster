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

class GeminiChatAssistant(
    private val context: Context,
    private val keyStore: GeminiKeyStore,
    private val createApi: (String) -> GeminiAPI
) : ChatAssistant {
    override suspend fun generate(messages: List<Message>): String {
        val key = keyStore.read()?.takeIf { it.isNotBlank() } ?: throw MissingApiKeyException()
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val capabilities = connectivity.getNetworkCapabilities(network)
        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true ||
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) throw OfflineException()
        return createApi(key).generate(messages.map {
            GeminiContent(
                parts = listOf(GeminiPart(text = it.text)),
                role = if (it.role == Author.USER) "user" else "model"
            )
        })
    }
}
