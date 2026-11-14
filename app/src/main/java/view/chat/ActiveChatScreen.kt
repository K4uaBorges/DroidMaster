package view.chat

import DroidMaster.apps.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import model.Author

private val TopFloatActionsBubbleColor = Color(0xFFFFE6DC)

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
                    conversation = state.messages,
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
            state.error?.let { Text(text = it, modifier = Modifier.padding(12.dp)) }
            if (state.error != null && !state.isSending && state.messages.lastOrNull()?.role == model.Author.USER) {
                androidx.compose.material3.TextButton(onClick = { controller.retryResponse() }) { Text("Tentar novamente") }
            }
            if (state.error != null) androidx.compose.material3.TextButton(onClick = onSettingsClick) { Text("Definições") }
            if (state.isSending) Text("A aguardar resposta...", modifier = Modifier.padding(12.dp))
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
            color = TopFloatActionsBubbleColor,
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
            color = TopFloatActionsBubbleColor,
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

    LaunchedEffect(conversation.size) {
        if(conversation.isNotEmpty()) {
            listState.animateScrollToItem(
                conversation.lastIndex
            )
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
        itemsIndexed(conversation) { index, message ->
            MessageView(
                message = message,
                showFullDate = showDayBubble[index]
            )
        }
    }

//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(
//                horizontal = 12.dp,
//                vertical = 8.dp
//            ),
//        verticalArrangement = Arrangement.Bottom
//    ) {
//        for (message in conversation) {
//            MessageView(
//                text = message.text,
//                isUser = message.isUser,
//                sentAt = message.sentAt
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//    }
}
