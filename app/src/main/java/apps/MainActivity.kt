package apps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import controller.ChatController
import view.chat.ActiveChatScreenMain
import view.history.ChatHistoryScreenMain

class MainActivity : ComponentActivity() {

    private val droidMasterApplication: DroidMasterApplication
        get() = application as DroidMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMasterApp(
                chatController = droidMasterApplication.chatController
            )
        }
    }
}

enum class Screen{
    TITLE,
    ACTIVE_CHAT,
    HISTORY,
    SETTINGS,
    ABOUT
}

@Composable
fun DroidMasterApp(
    chatController: ChatController
) {

    var currentScreen by remember {
        mutableStateOf(Screen.ACTIVE_CHAT)
    }

    when (currentScreen) {
        Screen.ACTIVE_CHAT -> {
            ActiveChatScreenMain(
                controller = chatController,
                onHistoryClick = {
                    currentScreen = Screen.HISTORY
                }
            )
        }

        Screen.HISTORY -> {
            ChatHistoryScreenMain(
                onBackClick = {
                    currentScreen = Screen.ACTIVE_CHAT
                }
            )
        }

        else -> currentScreen = Screen.ACTIVE_CHAT
    }
}