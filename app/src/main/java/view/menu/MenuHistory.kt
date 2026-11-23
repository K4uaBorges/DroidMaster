package view.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import model.Conversation
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.key

@Composable
fun MenuHistoryView(
    conversations: List<Conversation>,
    onConversationClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = conversations,
            key = { it.id.toString() }

        ) { conversation ->

            MenuConversationItemView(
                conversation = conversation,
                isTemporary = false,
                onClick = {
                    onConversationClick(conversation.id)
                }
            )
        }
    }
}