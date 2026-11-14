package view.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.getValue

private val InputBubbleColor = Color(0xFFFFE6DC)
private val InputTextColor = Color.Black
private val InputPlaceholderColor = Color(0xFF8F7D76)
private val SendButtonColor = Color(0xFFFF7A59)                     // Color(0xFFA84F35)
private val SendIconColor = Color.White
private val DisabledSendIconColor = Color.Black
private val DisabledSendButtonColor = Color.Transparent             // Color(0xFFFF7A59)

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Escrever a mensagem... ",
    enabled: Boolean = true
) {
    val canSend = value.isNotBlank() && enabled

    val animatedButtonColor by animateColorAsState(
        targetValue = if (canSend)
            SendButtonColor
        else
            DisabledSendButtonColor,
        animationSpec = tween(durationMillis = 300),
        label = "SendButtonColor"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (canSend)
            SendIconColor
        else
            DisabledSendIconColor,
        animationSpec = tween(durationMillis = 300),
        label = "SendIconColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        color = InputBubbleColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = InputPlaceholderColor,
                        fontSize = 20.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = false,
                    maxLines = 4,
                    textStyle = TextStyle(
                        color = InputTextColor,
                        fontSize = 18.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) onSend()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 28.dp)
                )
            }

            IconButton(
                onClick = {
                    if (canSend) onSend()
                },
                enabled = enabled,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(animatedButtonColor)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Enviar Mensagem",
                    tint = animatedIconColor
                )
            }
        }
    }
}