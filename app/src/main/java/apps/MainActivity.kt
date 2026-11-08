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
import controller.ConversationController
import controller.SharedController
import view.chat.ActiveChatScreenMain
import view.history.ChatHistoryScreenMain
import view.title.TitleScreenMain
import view.settings.SettingsScreen
import storage.GeminiKeyStore
import view.about.AboutMeScreen

class MainActivity : ComponentActivity() {

    private val droidMasterApplication: DroidMasterApplication
        get() = application as DroidMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMasterApp(
                chatController = droidMasterApplication.chatController,
                conversationController = droidMasterApplication.conversationController,
                sharedController = droidMasterApplication.sharedController,
                keyStore = droidMasterApplication.geminiKeyStore
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
    chatController: ChatController,
    conversationController: ConversationController,
    sharedController: SharedController,
    keyStore: GeminiKeyStore
) {

    var currentScreen by remember {
        mutableStateOf(Screen.TITLE)
    }

    var previousScreen by remember {
        mutableStateOf(Screen.TITLE)
    }

    when (currentScreen) {
        Screen.TITLE -> {
            TitleScreenMain(
                controller = chatController,
                onHistoryClick = {
                    currentScreen = Screen.HISTORY
                    previousScreen = Screen.TITLE
                },
                onStartChatClick = {
                    currentScreen = Screen.ACTIVE_CHAT
                },
                onSettingsClick = {
                    previousScreen = Screen.TITLE
                    currentScreen = Screen.SETTINGS
                },
                onTemporaryChatClick = {
                    if (chatController.startTemporaryConversation()) {
                        currentScreen = Screen.ACTIVE_CHAT
                    }
                }
            )
        }

        Screen.ACTIVE_CHAT -> {
            ActiveChatScreenMain(
                controller = chatController,
                conversationId = conversationController.screenState.currentConversationId,
                sharedController = sharedController,
                onHistoryClick = {
                    currentScreen = Screen.HISTORY
                    previousScreen = Screen.ACTIVE_CHAT
                },
                onSettingsClick = { previousScreen = Screen.ACTIVE_CHAT; currentScreen = Screen.SETTINGS }
            )
        }

        Screen.HISTORY -> {
            ChatHistoryScreenMain(
                conversations = conversationController
                    .screenState
                    .conversations,
                onConversationClick = { conversationId ->
                    chatController.openConversation(conversationId)
                    currentScreen = Screen.ACTIVE_CHAT
                },
                onNewConversationClick = {
                    chatController.newConversation()
                    currentScreen = Screen.TITLE
                },
                onBackClick = {
                    currentScreen = previousScreen
                },
                onAboutClick = {
                    currentScreen = Screen.ABOUT
                },
                errorMessage = conversationController.screenState.error
            )
        }

        Screen.SETTINGS -> SettingsScreen(keyStore) { currentScreen = previousScreen }
        Screen.ABOUT -> AboutMeScreen(
            onBackClick = {
                currentScreen = Screen.HISTORY
            }
        )
        else -> currentScreen = Screen.TITLE
    }
}
