package view.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

data class DroidMasterTheme(
    val colors: DroidMasterColors,
    val animations: DroidMasterAnimations
)

val LocalDroidMasterTheme =
    staticCompositionLocalOf<DroidMasterTheme> {        // "Quero criar um CompositionLocal que vai transportar um objeto do tipo DroidMasterTheme."
        DroidMasterTheme(
            colors = DroidMasterPalettes.main,          // Valores por defeito.
            animations = DroidMasterAnimations()
        )
    }

@Composable
fun DroidMasterThemeProvider(
    theme: DroidMasterTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(                           // Este é o componente que coloca um valor dentro do contexto da árvore Compose.
        LocalDroidMasterTheme provides theme,
        content = content
    )
}