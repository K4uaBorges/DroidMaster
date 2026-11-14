package view.title

import DroidMaster.apps.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controller.ChatController
import view.chat.ChatInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color


@Composable
fun TitleScreenMain(
    controller: ChatController,
    onMenuClick: () -> Unit,
    onStartChatClick: () -> Unit,
//    onSettingsClick: () -> Unit,
    onTemporaryChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = controller.screenState

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Surface(
                onClick = onMenuClick,
                shape = CircleShape,
                color = Color(0xFFFFE6DC),
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

            Surface(
                onClick = onTemporaryChatClick,
                enabled = !state.isSending,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = CircleShape,
                color = Color(0xFFFFE6DC),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (controller.isTemporaryChat) {
                            Icons.Default.WatchLater
                        } else {
                            Icons.Default.Schedule
                        },
                        contentDescription = if (controller.isTemporaryChat){
                            "Conversa temporária ativada"
                        } else {
                            "Ativar conversa temporária"
                        },
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 26.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.droidmaster_logo
                    ),
                    contentDescription = "DroidMaster",
                    modifier = Modifier.height(90.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Como posso ajudar?", // Trocar para ser aleatório as frases.
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    modifier =  Modifier.padding(
                        top = 24.dp,
                        bottom = 24.dp
                    )
                )

                ChatInput(
                    value = state.messageText,
                    onValueChange = {
                        controller.changeMessageText(it)
                    },
                    onSend = {
                        if ( state.messageText.isNotBlank() && !state.isSending) {
                            controller.sendMessage()
                            onStartChatClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Pergunte ao DroidMaster...",
                    enabled = !state.isSending
                )
                TextButton(
                    onClick = onTemporaryChatClick,
                    enabled = !state.isSending,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("OneView · Conversa temporária")
                }

            }

        }
    }
}
