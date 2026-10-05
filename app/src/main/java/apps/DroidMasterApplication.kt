package apps

import android.app.Application
import controller.ChatController
import kotlin.getValue

class DroidMasterApplication: Application() {

    val chatController by lazy {
        ChatController()
    }
}