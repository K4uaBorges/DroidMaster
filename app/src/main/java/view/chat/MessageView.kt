package view.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.Author
import model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val UserBubbleColor = Color(0xFF6E56CF)      // roxo
private val DroidMasterBubbleColor = Color(0xFFFF7A59) // coral/laranja
private val TextColor = Color.White
private val TimeTextColor = Color(0xCCFFFFFF)

@Composable
fun MessageView(
    message: Message
) {

    val isUser = message.role == Author.USER

    val bubbleColor = if (isUser) UserBubbleColor else DroidMasterBubbleColor

    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = TextColor,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(4.dp)
            )

            if (isUser) {
                Text(
                    text = formatTime(message.timestamp),
                    color = TimeTextColor,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 3.dp)
                )
            }

        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp)
    )
}