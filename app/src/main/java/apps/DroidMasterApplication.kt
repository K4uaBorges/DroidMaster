package apps

import android.app.Application
import controller.ChatController
import controller.ConversationController
import kotlin.getValue

class DroidMasterApplication : Application() {

    val conversationController by lazy {
        ConversationController()
    }

    val chatController by lazy {
        ChatController(
            conversationController
        )
    }
}
