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
import androidx.compose.ui.Modifier
import android.widget.Toast
import controller.SharedController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import model.Shared
import kotlin.uuid.Uuid

@Composable
fun SharedButton(
    conversationId: Uuid,
    destination: Shared,
    controller: SharedController,
    modifier: Modifier = Modifier
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
                try {
                    controller.saveToUri(context, uri, conversationId)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    Toast.makeText(context, "Não foi possível guardar a conversa.", Toast.LENGTH_LONG).show()
                }
            }
        }

    IconButton(
        modifier = modifier,
        onClick = {

            when (destination) {

                is Shared.People -> {

                    scope.launch {
                        try {
                            controller.shareWithPeople(context, conversationId)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            Toast.makeText(context, "Não foi possível partilhar a conversa.", Toast.LENGTH_LONG).show()
                        }
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
            modifier = modifier,
            contentDescription = "Partilhar conversa"
        )
    }
}
