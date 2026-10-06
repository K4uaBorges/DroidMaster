package view.share

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import controller.shareWithPeople
import controller.shared.saveConversationToUri
import kotlinx.coroutines.launch
import model.Shared
import storage.dao.MessageDao
import kotlin.uuid.Uuid

@Composable
fun SharedButton(
    conversationId: Uuid,
    destination: Shared,
    messageDao: MessageDao
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val createFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "text/markdown"
            )
        ) { uri ->

            uri ?: return@rememberLauncherForActivityResult

            scope.launch {

                saveConversationToUri(
                    context = context,
                    uri = uri,
                    conversationId = conversationId,
                    messageDao = messageDao
                )
            }
        }

    IconButton(
        onClick = {

            when (destination) {

                is Shared.People -> {

                    scope.launch {

                        shareWithPeople(
                            context = context,
                            conversationId = conversationId,
                            messageDao = messageDao
                        )
                    }
                }

                is Shared.File -> {

                    createFileLauncher.launch(
                        "conversation_$conversationId.md"
                    )
                }
            }
        }
    ) {

        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Partilhar conversa"
        )
    }
}