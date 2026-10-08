package errors

sealed class Error(msg : String) : Exception(msg)

class GeminiHttpException(val statusCode: Int) : Error("Gemini HTTP $statusCode")
class GeminiEmptyResponseException : Error("O Gemini não devolveu uma resposta de texto.")
