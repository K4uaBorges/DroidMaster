package view.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.Author
import model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import com.swmansion.enriched.markdown.compose.EnrichedMarkdownText
import com.swmansion.enriched.markdown.compose.material3.rememberMaterialMarkdownStyle
import com.swmansion.enriched.markdown.math.LatexMathPlugin
import com.swmansion.enriched.markdown.math.compose.math

private val UserBubbleColor = Color(0xFFFF7A59)
private val TextColor = Color.White
private val TimeTextColor = Color(0xFFA84F35)
private val TimeBubbleColor = Color(0xFFFFE6DC)
@Composable
fun MessageView(
    message: Message,
    showFullDate: Boolean = true
) {

    val isUser = message.role == Author.USER

    val bubbleShape =
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isUser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (showFullDate) 12.dp else 5.dp),
                horizontalArrangement = if (showFullDate)
                    Arrangement.Center
                else
                    Arrangement.End
            ) {
                Text(
                    text = if (showFullDate)
                        formatTime(message.timestamp)
                    else
                        formatHour(message.timestamp),

                    color = TimeTextColor,

                    fontSize = 14.sp,

                    modifier = if (showFullDate) {
                        Modifier
                            .background(
                                color = TimeBubbleColor,
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 6.dp
                            )
                    } else {
                        Modifier.padding(end = 4.dp)
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser)
                Arrangement.End
            else
                Arrangement.Start
        ) {
            Column(
                modifier = if (isUser) {
                    Modifier
                        .widthIn(max = 280.dp)
                        .clip(bubbleShape)
                        .background(UserBubbleColor)
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 4.dp,
                            vertical = 8.dp
                        )
                }

            ) {
                if (isUser) {
                    Text(
                        text = message.text,
                        color = TextColor,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(4.dp)
                    )
                } else {
                    // Estilo das respostas do Gemini
                    val markdownStyle = rememberMaterialMarkdownStyle().merge {

                        math {
                            backgroundColor = Color.Transparent
                            color = Color.Black
                            fontSize = 22.sp
                            padding = 12.dp
                            marginBottom = 16.dp
                            alignment = Alignment.CenterHorizontally
                        }

                        codeBlock {
                            backgroundColor = TimeBubbleColor       // Color(0xFFFFE6DC)
                            color = Color.Black

                            borderColor = Color.Transparent
                            borderWidth = 0.dp

                            cornerRadius = 12.dp
                            padding = 12.dp

                            fontSize = 16.sp
                            lineHeight = 21.sp
                        }

                        paragraph {
                            fontSize = 18.sp
                            lineHeight = 32.sp
                            marginBottom = 22.dp
                        }

                        h1 {
                            fontSize = 28.sp
                            lineHeight = 29.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        h2 {
                            fontSize = 26.sp
                            lineHeight = 27.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        h3 {
                            fontSize = 22.sp
                            lineHeight = 25.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        h4 {
                            fontSize = 20.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        h5 {
                            fontSize = 18.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        h6 {
                            fontSize = 18.sp
                            fontWeight = FontWeight.SemiBold
                        }

                        list {
                            fontSize = 18.sp
                            lineHeight = 32.sp
                            marginBottom = 18.dp
                        }
                    }

                    LatexMathPlugin {                       // Ativa o suporte às fórmulas LaTeX.
                        EnrichedMarkdownText(
                            markdown = message.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            style = markdownStyle
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat(
        "EEEE, dd/MM/yyyy · HH:mm",
        Locale.forLanguageTag("pt-PT")
    ).format(Date(timestamp))
        .replaceFirstChar { it.uppercase() }
}

private fun formatHour(timestamp: Long): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.forLanguageTag("pt-PT")
    ).format(Date(timestamp))
}