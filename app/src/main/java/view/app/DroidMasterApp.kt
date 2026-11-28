package view.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import controller.ChatController
import controller.ConversationController
import controller.SharedController
import storage.GeminiKeyStore
import view.about.AboutMeScreen
import view.chat.ActiveChatScreenMain
import view.menu.MenuDrawerView
import view.settings.SettingsScreen
import view.title.TitleScreenMain

@Composable
fun DroidMasterAppView(
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

    var isMenuOpen by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {

            when (currentScreen) {

                Screen.TITLE -> {
                    TitleScreenMain(
                        controller = chatController,
                        onMenuClick = {
                            isMenuOpen = true
                        },
                        onStartChatClick = {
                            currentScreen = Screen.ACTIVE_CHAT
                        },
                        onTemporaryChatClick = {
                            chatController.toggleTemporaryMode()
                        }
                    )
                }

                Screen.ACTIVE_CHAT -> {
                    ActiveChatScreenMain(
                        controller = chatController,
                        conversationId = conversationController.screenState.currentConversationId,
                        sharedController = sharedController,
                        onHistoryClick = {
                            isMenuOpen = true
                        },
                        onSettingsClick = {
                            previousScreen = Screen.ACTIVE_CHAT
                            currentScreen = Screen.SETTINGS
                        }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(keyStore) {
                        currentScreen = previousScreen
                    }
                }

                Screen.ABOUT -> {
                    AboutMeScreen(
                        onBackClick = {
                            currentScreen = previousScreen
                        }
                    )
                }
            }

            MenuDrawerView(
                isOpen = isMenuOpen,
                onClose = {
                    isMenuOpen = false
                },
                conversations = conversationController.screenState.conversations,
                onConversationClick = { conversationId ->
                    chatController.openConversation(conversationId)
                    currentScreen = Screen.ACTIVE_CHAT
                    isMenuOpen = false
                },
                onNewChatClick = {
                    currentScreen = Screen.TITLE
                    isMenuOpen = false
                    if (chatController.isTemporaryChat) {
                        chatController.exitTemporaryChat()
                    } else {
                        chatController.newConversation()
                    }
                },
                onTitleScreenClick = {
                    currentScreen = Screen.TITLE
                    isMenuOpen = false
                    if (chatController.isTemporaryChat) {
                        chatController.exitTemporaryChat()
                    } else {
                        chatController.newConversation()
                    }
                },
                onSettingsClick = {
                    currentScreen = Screen.SETTINGS
                    isMenuOpen = false                      // PARA SER RETIRADO PORQUE AS DEFINIÇÕES SERÃO DENTRO DO MENU.
                },
                onAboutMeClick = {
                    currentScreen = Screen.ABOUT
                    isMenuOpen = false                      // PARA SER RETIRADO PORQUE O ABOUT SERÁ DENTRO DO MENU.
                }
            )
        }
    }
}