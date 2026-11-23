package view.title

import DroidMaster.apps.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import view.theme.LocalDroidMasterTheme

@Composable
fun TitleScreenMain(
    controller: ChatController,
    onMenuClick: () -> Unit,
    onStartChatClick: () -> Unit,
    onTemporaryChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = controller.screenState

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopFloatingActions(
                onMenuClick = onMenuClick,
                onTemporaryChatClick = onTemporaryChatClick,
                controller = controller,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-50).dp)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                MiddleTextAndImage()

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                ChatInput(
                    value = state.messageText,
                    onValueChange = {
                        controller.changeMessageText(it)
                    },
                    onSend = {
                        if (state.messageText.isNotBlank() && !state.isSending) {
                            controller.sendMessage()
                            onStartChatClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Perguntar ao DroidMaster...",
                    enabled = !state.isSending
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                SuggestedTopicsView(
                    onSuggestedTopicClick = { prompt ->
                        if (!controller.screenState.isSending) {
                            controller.changeMessageText(prompt)

                            if (controller.sendMessage() != null) {
                                onStartChatClick()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun TopFloatingActions(
    onMenuClick: () -> Unit,
    onTemporaryChatClick: () -> Unit,
    controller: ChatController,
    modifier: Modifier = Modifier,
) {
    val theme = LocalDroidMasterTheme.current

    val animatedButtonColor by animateColorAsState(
        targetValue = if (controller.isTemporaryChat)
            theme.colors.buttonColor
        else
            theme.colors.disabledButtonColor,
        animationSpec = tween(durationMillis = 300),
        label = "SendButtonColor"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (controller.isTemporaryChat)
            theme.colors.iconColor
        else
            theme.colors.disabledIconColor,
        animationSpec = tween(durationMillis = 300),
        label = "SendIconColor"
    )

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
                    modifier = Modifier.size(32.dp),
                    tint = theme.colors.disabledIconColor,
                    contentDescription = "Abrir Menu"
                )
            }
        }

        // Botão chat temporário em círculo.
        Surface(
            onClick = onTemporaryChatClick,
            shape = CircleShape,
            color = animatedButtonColor,
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
                    modifier = Modifier.size(32.dp),
                    tint = animatedIconColor
                )
            }
        }
    }
}

@Composable
fun MiddleTextAndImage(
    modifier: Modifier = Modifier
) {

    val greeting = remember {
        TitleGreetings.random()
    }

    Column(
        modifier = modifier
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
            modifier = Modifier.height(120.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = greeting,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = 24.dp,
                bottom = 24.dp
            ),
        )
    }
}