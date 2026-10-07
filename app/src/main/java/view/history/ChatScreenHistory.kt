package view.history

import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ChatHistoryScreenMain(
    conversations: List<Conversation>,
    onConversationClick: (Long) -> Unit,
    onNewConversationClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Histórico de Conversas")

            Button(
                onClick = onNewConversationClick,
                modifier = Modifier
                    .padding(
                        vertical = 8.dp
                    )
            ) {
                Text("Nova Conversa")
            }

            if (conversations.isEmpty()) {

                Text(
                    text = "Ainda não existem conversas.",
                    modifier = Modifier
                        .padding(
                            vertical = 16.dp
                        )
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                onConversationClick(conversation.id)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onBackClick
            ) {
                Text("Voltar")
            }
        }
    }
}

@Composable
private fun  ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = conversation.title
            )

            Text(
                text = formatDate(conversation.createdAt)
            )
        }
    }
}

private fun formatDate(timestamp: Long) : String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}