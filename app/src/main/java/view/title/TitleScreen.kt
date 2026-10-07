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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controller.ChatController

@Composable
fun TitleScreenMain(
    controller: ChatController,
    onHistoryClick: () -> Unit,
    onStartChatClick: () -> Unit,
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
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "☰",
                    fontSize = 26.sp
                )
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

                OutlinedTextField(
                    value = state.messageText,
                    onValueChange = {
                        controller.changeMessageText(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Pergunte ao DroidMaster...")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (state.messageText.isNotBlank()) {
                                controller.sendMessage()
                                onStartChatClick()
                            }
                        }
                    ),
                    trailingIcon = {
                        Button(
                            onClick = {
                                controller.sendMessage()
                                onStartChatClick()
                            },
                            enabled = state.messageText.isNotBlank()
                        ) {
                            Text(">")
                        }
                    }
                )

            }

        }
    }
}