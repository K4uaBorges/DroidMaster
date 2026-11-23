package view.menu

import DroidMaster.apps.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.theme.LocalDroidMasterTheme

@Composable
fun MenuActionsView(
    onNewChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutMeClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val theme = LocalDroidMasterTheme.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            onClick = onNewChatClick,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = theme.colors.tertiaryColor
        ) {

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(R.drawable.edit_square),
                    contentDescription = "Nova Conversa",
                    tint = theme.colors.secondaryColor,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Chat",
                    color = theme.colors.secondaryColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = theme.colors.secondaryColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Definições",
                        modifier = Modifier.size(32.dp),
                        tint = theme.colors.tertiaryColor
                    )
                }

                IconButton(
                    onClick = onAboutMeClick,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About Me",
                        modifier = Modifier.size(32.dp),
                        tint = theme.colors.tertiaryColor
                    )
                }
            }
        }
    }
}