package apps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import view.app.DroidMasterAppView


class MainActivity : ComponentActivity() {

    private val droidMasterApplication: DroidMasterApplication
        get() = application as DroidMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMasterAppView(
                chatController = droidMasterApplication.chatController,
                conversationController = droidMasterApplication.conversationController,
                sharedController = droidMasterApplication.sharedController,
                keyStore = droidMasterApplication.geminiKeyStore
            )
        }
    }
}
