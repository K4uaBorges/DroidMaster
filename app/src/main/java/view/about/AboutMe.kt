package view.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutMeScreen(onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "About me",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "DroidMaster",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "O DroidMaster é um assistente que te acompanha na " +
                        "aprendizagem de programação Android. Podes esclarecer " +
                        "dúvidas, explorar conceitos de Kotlin e Jetpack Compose " +
                        "e pedir ajuda para compreender o teu código."
            )

            Text(
                text = "Como funciona",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Através da inteligência artificial do Gemini, o " +
                        "DroidMaster gera respostas com base nas tuas perguntas " +
                        "e no contexto da conversa, permitindo aprofundar cada " +
                        "tema passo a passo."
            )

            Text(
                text = "As tuas conversas",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "O histórico fica guardado neste dispositivo para " +
                        "poderes consultar explicações anteriores e retomar " +
                        "uma conversa quando precisares."
            )

            Text(
                text = "Para começar",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Configura a tua chave da API Gemini nas Definições " +
                        "e inicia uma conversa. Para obter respostas, precisas " +
                        "de uma ligação à Internet."
            )

            Text(
                text = "Aprende, experimenta e confirma",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Usa as respostas como apoio ao estudo: experimenta " +
                        "os exemplos e confirma as sugestões na documentação " +
                        "oficial. A inteligência artificial também pode cometer erros."
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onBackClick) {
                Text(text = "Voltar")
            }
        }
    }
}