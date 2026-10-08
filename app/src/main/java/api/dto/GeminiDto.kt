package api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@Serializable
data class GeminiPart(
    val text: String = "",
    val thought: Boolean = false
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
