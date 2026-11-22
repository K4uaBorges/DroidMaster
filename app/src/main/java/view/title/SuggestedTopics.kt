package view.title

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.theme.LocalDroidMasterTheme

data class SuggestedTopic(
    val title: String,
    val prompt: String
)

private object SuggestedTopics {
    val list = listOf(
        SuggestedTopic(
            "Aprender Kotlin",
            "Explica-me os conceitos principais da linguagem Kotlin."
        ),
        SuggestedTopic(
            "Ideias de Projetos",
            "Sugere-me ideias para projetos de programação."
        ),
        SuggestedTopic(
            "Explicar Matéria",
            "Explica-me o tema que te pergunto na pergunta a seguir, de forma consistente, simples e intuítiva."
        ),
        SuggestedTopic(
            "Organizar Tarefas",
            "Preciso de ajuda para organizar as tarefas que te apresento na minha próxima mensagem."
        ),
        SuggestedTopic(
            "Resumir Email",
            "Resume o email que te envio na próxima mensagem."
        ),
        SuggestedTopic(
            "Escrever Texto",
            "Escreve um texto composto, de escrita humana, e bem estruturado sobre o tema que te envio na próxima mensagem."
        ),
        SuggestedTopic(
            "Plano de Estudo",
            "Cria-me um plano de estudo elaborado sobre o que te envio na próxima mensagem."
        ),
        SuggestedTopic(
            "Ajuda com Código",
            "Ajuda-me a entender e, se possível, melhorar o código que envio a seguir."
        ),
        SuggestedTopic(
            "Aprender Python",
            "Explica-me os fundamentos da linguagem Python, com exemplos práticos."
        ),
        SuggestedTopic(
            "Aprender Java",
            "Ensina-me os principais conceitos da linguagem Java, com exemplos."
        ),
        SuggestedTopic(
            "Aprender SQL",
            "Explica-me os fundamentos de SQL e como trabalhar com bases de dados."
        ),
        SuggestedTopic(
            "Desenvolvimento Web",
            "Explica-me como funciona o desenvolvimento de aplicações Web, desde o frontend até ao backend."
        ),
        SuggestedTopic(
            "Explicar Git",
            "Explica-me como utilizar Git e GitHub para gerir projetos de programação."
        ),
        SuggestedTopic(
            "Explicar Algoritmos",
            "Explica-me o algoritmo que vou indicar, de forma simples e com exemplos."
        ),
        SuggestedTopic(
            "Criar Questionário",
            "Cria um questionário para testar os meus conhecimentos sobre o tema que vou indicar."
        ),
        SuggestedTopic(
            "Criar Checklist",
            "Cria uma lista de verificação organizada para a atividade que vou indicar."
        ),
        SuggestedTopic(
            "Escrever Email",
            "Ajuda-me a escrever um email claro, educado e adequado à situação que vou explicar."
        )
    )

    fun random(): List<SuggestedTopic> {
        return list.shuffled().take(4)
    }
}

@Composable
fun SuggestedTopicsView(
    onSuggestedTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalDroidMasterTheme.current

    val topics = remember {
        SuggestedTopics.random()
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        topics.forEach { topic ->

            Surface(
                onClick = {
                    onSuggestedTopicClick(topic.prompt)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = theme.colors.primary
                ),
                color = theme.colors.bubbleColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = topic.title,
                        color = theme.colors.primary,
                        fontSize = 18.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}