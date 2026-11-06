package view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import storage.GeminiKeyStore

@Composable
fun SettingsScreen(keyStore: GeminiKeyStore, onBack: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Definições", style = MaterialTheme.typography.headlineMedium)
        Text("Insere a tua chave da API Gemini. A chave é guardada cifrada neste dispositivo.")
        OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("API Key Gemini") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    try { keyStore.save(key); key = ""; message = "Chave guardada." }
                    catch (_: Exception) { message = "Não foi possível guardar a chave." }
                }
            }, enabled = key.isNotBlank()) { Text("Guardar") }
            OutlinedButton(onClick = {
                scope.launch {
                    try { keyStore.save(""); key = ""; message = "Chave removida." }
                    catch (_: Exception) { message = "Não foi possível remover a chave." }
                }
            }) { Text("Remover chave") }
        }
        message?.let { Text(it) }
        TextButton(onClick = onBack) { Text("Voltar") }
    }
}
