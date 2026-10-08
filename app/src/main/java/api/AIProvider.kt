package api

import api.dto.GeminiContent

interface AIProvider {
    suspend fun generate(contents: List<GeminiContent>): String
}