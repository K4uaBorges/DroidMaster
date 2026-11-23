package view.menu

import DroidMaster.apps.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import view.theme.LocalDroidMasterTheme


@Composable
fun MenuHeaderView(
    modifier: Modifier = Modifier,
    onLogoClick: () -> Unit
) {

    val theme = LocalDroidMasterTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(                                            // Bolha com o logótipo.
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = theme.colors.bubbleColor,
            onClick = onLogoClick
        ) {

            Box(
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.droidmaster_menu
                    ),
                    contentDescription = "DroidMaster Logo",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = theme.colors.bubbleColor
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisa de Conversas",
                    tint = theme.colors.tertiaryColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}