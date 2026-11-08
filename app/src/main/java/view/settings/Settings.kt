package view.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import storage.GeminiKeyStore

@Composable
fun SettingsScreen(keyStore: GeminiKeyStore, onBack: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var hasKey by remember { mutableStateOf<Boolean?>(null) }
    var isBusy by remember { mutableStateOf(true) }
    var confirmRemoval by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    BackHandler(onBack = onBack)

    LaunchedEffect(keyStore) {
        try {
            hasKey = !keyStore.read().isNullOrBlank()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            message = "Não foi possível consultar a chave guardada."
        } finally {
            isBusy = false
        }
    }

    fun saveKey(value: String) {
        isBusy = true
        message = null

        scope.launch {
            try {
                keyStore.save(value)
                hasKey = value.isNotBlank()
                key = ""
                showKey = false
                message = if (value.isBlank()) {
                    "Chave removida."
                } else {
                    "Chave guardada."
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                message = "Não foi possível alterar a chave. Tenta novamente."
            } finally {
                isBusy = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Definições",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "API Gemini",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = when {
                    isBusy -> "A processar…"
                    hasKey == true -> "Chave configurada neste dispositivo."
                    hasKey == false -> "Ainda não tens uma chave configurada."
                    else -> "Estado da chave indisponível."
                }
            )

            Text(
                text = "A chave é guardada cifrada neste dispositivo. " +
                        "Para a substituir, introduz uma nova chave e guarda."
            )

            OutlinedTextField(
                value = key,
                onValueChange = {
                    key = it
                    message = null
                },
                label = { Text("Nova chave API Gemini") },
                singleLine = true,
                enabled = !isBusy,
                visualTransformation = if (showKey) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = { showKey = !showKey },
                        enabled = !isBusy
                    ) {
                        Text(if (showKey) "Ocultar" else "Mostrar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { saveKey(key.trim()) },
                enabled = key.isNotBlank() && !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar chave")
            }

            OutlinedButton(
                onClick = { confirmRemoval = true },
                enabled = !isBusy && hasKey != false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remover chave")
            }

            message?.let {
                Text(text = it)
            }

            TextButton(onClick = onBack) {
                Text("Voltar")
            }
        }
    }

    if (confirmRemoval) {
        AlertDialog(
            onDismissRequest = { confirmRemoval = false },
            title = { Text("Remover chave?") },
            text = {
                Text(
                    "Deixas de poder obter respostas do Gemini até " +
                            "configurares outra chave. O histórico mantém-se."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemoval = false
                        saveKey("")
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmRemoval = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}