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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveChatScreenMain(
    controller: ChatController,
    onHistoryClick: () -> Unit,         // "ActiveChatScreen, quando alguém quiser abrir o histórico, executa esta função."
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

            ChatHeader(
                onMenuClick = onHistoryClick
            )

            ChatMessages(
                conversation = state.messages,
                modifier = Modifier.weight(1f)
            )

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
                    }
                )
            }
        }
    }
}

@Composable
fun ChatHeader(
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = "☰",
                fontSize = 26.sp
            )
        }

        Image(
            painter = painterResource(
                id = R.drawable.droidmaster_logo
            ),
            contentDescription = "DroidMaster",
            modifier = Modifier
                .align(Alignment.Center)
                .height(60.dp),
            contentScale = ContentScale.Fit
        )
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

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.Bottom
        )
    ) {
        items(conversation) { message ->
            MessageView(
                message = message
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

private fun currentHourMinute(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
