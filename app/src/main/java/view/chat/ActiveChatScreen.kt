package view.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import controller.ChatController
import controller.SharedController
import model.Message
import model.Shared
import view.share.SharedButton
import kotlin.uuid.Uuid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import model.Author
import view.theme.LocalDroidMasterTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ActiveChatScreenMain(
    controller: ChatController,
    conversationId: Uuid?,
    sharedController: SharedController,
    onHistoryClick: () -> Unit,         // "ActiveChatScreen, quando alguém quiser abrir o histórico, executa esta função."
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = controller.screenState

    val visibleMessages = rememberPresentedMessages(            // Estamos a verificar se a mensagem provisória ainda não existe entre as mensagens concluídas.
        messages = state.messages,
        streamingMessage = state.streamingMessage,
        error = state.error
    )

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ChatMessages(
                    conversation = visibleMessages,
                    modifier = Modifier.fillMaxSize()
                )

                TopFloatingActions(
                    onMenuClick = onHistoryClick,
                    conversationId = conversationId,
                    sharedController = sharedController
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                ChatInput(
                    value = state.messageText,
                    onValueChange = {
                        controller.changeMessageText(it)
                    },
                    onSend = {
                        controller.sendMessage()
                    },
                    enabled = !state.isSending
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (
                state.error != null &&
                !state.isSending &&
                state.messages.lastOrNull()?.role == Author.USER
            ) {
                TextButton(
                    onClick = { controller.retryResponse() }
                ) { Text("Tentar novamente") }
            }

            if (state.error != null)
                TextButton(
                    onClick = onSettingsClick
                ) { Text("Definições") }

            if (state.isSending && state.streamingMessage == null)
                Text(
                    "A aguardar resposta...",
                    modifier = Modifier.padding(12.dp)
                )
        }
    }
}

@Composable
fun TopFloatingActions(
    onMenuClick: () -> Unit,
    conversationId: Uuid?,
    sharedController: SharedController,
    modifier: Modifier = Modifier
) {

    val theme = LocalDroidMasterTheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {

        // Botão menu em círculo.
        Surface(
            onClick = onMenuClick,
            shape = CircleShape,
            color = theme.colors.secondaryColor,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    modifier = modifier.size(28.dp),
                    contentDescription = "Abrir Menu"
                )
            }
        }

        // Botão partilha em círculo.
        Surface(
            shape = CircleShape,
            color = theme.colors.secondaryColor,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (conversationId != null) {
                    SharedButton(
                        conversationId = conversationId,
                        destination = Shared.People(),
                        controller = sharedController,
                        modifier = modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessages(
    conversation: List<Message>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(
        conversation.lastOrNull()?.conversationId,
        conversation.lastOrNull()?.order
        ) {
        if(conversation.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val showDayBubble = remember(conversation) {
        val seenDays = mutableSetOf<String>()
            // Guarda os dias que já encontramos.

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        conversation.map { message ->
            if (message.role == Author.USER) {
                // Garante que apenas consideramos as mensagens do USER.

                val day = formatter.format(Date(message.timestamp))
                seenDays.add(day)
                    // Devolve true se for a primeira vez que encontramos esse dia.

            } else {
                false
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 84.dp,
            bottom = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.Bottom
        )
    ) {

        item(key = "chat-end") {
            Spacer(modifier = Modifier.height(1.dp))
        }

        itemsIndexed(
            items = conversation.asReversed(),
            key = { _, message ->
                "${message.conversationId}:${message.order}"
            }
        ) { index, message ->

            MessageView(
                message = message,
                showFullDate = showDayBubble[
                    conversation.lastIndex - index
                ]
            )
        }
    }
}

@Composable
private fun rememberPresentedMessages(
    messages : List<Message>,
    streamingMessage: Message?,
    error: String?
) : List<Message> {

    val theme = LocalDroidMasterTheme.current

    val lastMessageByUser = messages.lastOrNull {
        it.role == Author.USER
    }

    var lastPartial by remember(
        lastMessageByUser?.conversationId,
        lastMessageByUser?.order
    ) {
        mutableStateOf<Message?>(null)
    }

    val pendingMessage = streamingMessage ?: lastPartial?.takeIf {
        error == null &&
        lastMessageByUser?.conversationId == it.conversationId &&
        lastMessageByUser.order + 1 == it.order
    }

    val isPending = pendingMessage != null &&
        messages.none {
            it.conversationId == pendingMessage.conversationId &&
            it.order == pendingMessage.order
        }

    SideEffect {
        lastPartial = if (isPending) pendingMessage else null
    }

    val visibleMessages = if (
        isPending
    ) {
        messages + pendingMessage
    } else {
        messages
    }

    val target = visibleMessages.lastOrNull()?.takeIf {
        it.role == Author.MODEL
    }

    var displayedLength by remember(
        target?.conversationId,
        target?.order
    ) {
        mutableIntStateOf(
            if (
                isPending &&
                theme.animations.isEnabled
            ) {
                0
            } else {
                target?.text?.length ?: 0
            }
        )
    }

    val latestText by rememberUpdatedState(
        target?.text.orEmpty()
    )

    val latestPending by rememberUpdatedState(isPending)

    LaunchedEffect(
        target?.conversationId,
        target?.order,
        theme.animations.isEnabled
    ) {
        if (!theme.animations.isEnabled) {
            displayedLength = latestText.length
            return@LaunchedEffect
        }

        while (latestPending || displayedLength < latestText.length) {

            val remaining = latestText.length - displayedLength

            if (remaining > 0) {
                val step = maxOf(8, remaining / 12)

                displayedLength = minOf(
                    displayedLength + step,
                    latestText.length
                )
            }

            delay(40.milliseconds)
        }
    }

    if (target == null || !theme.animations.isEnabled)
        return visibleMessages

    val displayedText = target.text.take(displayedLength)

    return if (displayedText.isNotBlank()) {
        visibleMessages.dropLast(1) +
                target.copy(text = displayedText)
    } else {
        visibleMessages.dropLast(1)
    }
}