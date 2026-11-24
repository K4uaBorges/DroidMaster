package view.menu

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import model.Conversation
import view.theme.LocalDroidMasterTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.uuid.Uuid
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun MenuDrawerView(
    isOpen: Boolean,
    onClose: () -> Unit,
    onTitleScreenClick: () -> Unit,
    conversations: List<Conversation>,
    onConversationClick: (Uuid) -> Unit,
    onNewChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutMeClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val theme = LocalDroidMasterTheme.current

    val duration = if (theme.animations.isEnabled) {
        theme.animations.durationMillis
    } else 0

    BackHandler(                                        // Quando o botão "back" do android for pressioando.
        enabled = isOpen
    ) {
        onClose()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        if (isOpen) {                                   // Para quando clickamos fora do menu, ele fechar.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(1f / 8f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = null,       // Retiramos as animações do clickable.
                        indication = null,
                        onClick = onClose
                    )

            )
        }

        AnimatedVisibility(
            visible = isOpen,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(7f / 8f)
                .fillMaxHeight(),
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(duration)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(duration)
            )
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {

                val borderOffset = 3.dp

                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth(
                            align = Alignment.Start,
                            unbounded = true
                        )
                        .offset(x = - borderOffset)
                        .width(maxWidth + borderOffset),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 32.dp,
                        bottomEnd = 32.dp
                    ),
                    border = BorderStroke(
                        width = 3.dp,
                        color = theme.colors.tertiaryColor
                    ),
                    color = theme.colors.backgroundColor
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {

                        MenuHeaderView(
                            onLogoClick = onTitleScreenClick
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            color = theme.colors.bubbleColor
                        ) {

                            MenuHistoryView(
                                conversations = conversations,
                                onConversationClick = onConversationClick,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MenuActionsView(
                            onNewChatClick = onNewChatClick,
                            onSettingsClick = onSettingsClick,
                            onAboutMeClick = onAboutMeClick
                        )
                    }
                }
            }
        }
    }
}
