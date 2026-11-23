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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.getValue
import view.theme.LocalDroidMasterTheme

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

    val theme = LocalDroidMasterTheme.current

    val animatedButtonColor by animateColorAsState(
        targetValue = if (canSend)
            theme.colors.buttonColor
        else
            theme.colors.disabledButtonColor,
        animationSpec = tween(theme.animations.durationMillis),
        label = "SendButtonColor"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (canSend)
            theme.colors.iconColor
        else
            theme.colors.disabledIconColor,
        animationSpec = tween(theme.animations.durationMillis),
        label = "SendIconColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        color = theme.colors.bubbleColor,
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
                        color = theme.colors.placeholderColor,
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
                        color = theme.colors.inputTextColor,
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
                    .background(
                        if(theme.animations.isEnabled)
                            animatedButtonColor
                        else
                            theme.colors.buttonColor
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Enviar Mensagem",
                    tint =
                        if (theme.animations.isEnabled)
                            animatedIconColor
                        else
                            theme.colors.iconColor
                )
            }
        }
    }
}