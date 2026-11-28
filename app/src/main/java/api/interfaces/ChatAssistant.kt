package api.interfaces

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import model.Message

fun interface ChatAssistant {
    suspend fun generate(messages: List<Message>): String

    fun generateStream(
        messages: List<Message>
    ) : Flow<String> = flow {
        emit(
            generate(messages)
        )
    }
}