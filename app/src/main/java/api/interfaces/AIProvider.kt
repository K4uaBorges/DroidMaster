package api.interfaces

import api.SystemInstructions
import api.dto.GeminiContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AIProvider {
    suspend fun generate(
        contents: List<GeminiContent>,
        systemInstruction: String = SystemInstructions.ANDROID_MENTOR
    ) : String

    fun generateStream(
        contents: List<GeminiContent>,
        systemInstruction: String = SystemInstructions.ANDROID_MENTOR
    ) : Flow<String>
}