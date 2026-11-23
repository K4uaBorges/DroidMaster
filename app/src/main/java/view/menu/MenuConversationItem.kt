package view.menu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import model.Conversation
import view.theme.LocalDroidMasterTheme

@Composable
fun MenuConversationItemView(
    conversation: Conversation,
    isTemporary: Boolean,
    onClick: () -> Unit
) {

    val theme = LocalDroidMasterTheme.current

    Surface(
        onClick = onClick,
        color = theme.colors.backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = conversation.title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = theme.colors.textColor
            )

            if (isTemporary) {
                Icon(
                    imageVector = Icons.Default.WatchLater,
                    contentDescription = "Conversa temporária",
                    tint = theme.colors.tertiaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}