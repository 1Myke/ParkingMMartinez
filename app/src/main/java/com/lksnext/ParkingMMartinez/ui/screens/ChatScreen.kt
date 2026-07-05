package com.lksnext.ParkingMMartinez.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.components.LksButton
import com.lksnext.ParkingMMartinez.ui.components.LksTextField
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.darkBlue
import com.lksnext.ParkingMMartinez.ui.theme.paleOrange
import com.lksnext.ParkingMMartinez.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var textoUsuario by remember { mutableStateOf("") }
    val cargando by viewModel.cargando.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.mensajes.size, cargando) {
        if (viewModel.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.mensajes.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LksOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Asistente",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = LksOrange
                    )
                }
                IconButton(onClick = { viewModel.resetChat() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Chat",
                        tint = LksOrange
                    )
                }
            }
        },
        bottomBar = {
            ChatInputArea(
                textoUsuario = textoUsuario,
                cargando = cargando,
                onTextoChange = { textoUsuario = it },
                onEnviarClick = {
                    if (textoUsuario.isNotBlank()) {
                        viewModel.enviarPregunta(textoUsuario)
                        textoUsuario = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.mensajes.isEmpty()) {
                ChatEmptyState(onSuggestionClick = { textoUsuario = it })
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(viewModel.mensajes) { mensaje ->
                        ChatMessageBubble(mensaje = mensaje)
                    }
                    if (cargando) {
                        item { ChatLoadingIndicator() }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyState(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🤖", fontSize = 54.sp, modifier = Modifier.padding(bottom = 12.dp))
        Text(
            text = stringResource(R.string.chat_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3142)
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.chat_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        val sugerencias = listOf(
            stringResource(R.string.chat_sug_1),
            stringResource(R.string.chat_sug_2),
            stringResource(R.string.chat_sug_3),
            stringResource(R.string.chat_sug_4),
            stringResource(R.string.chat_sug_5),
            stringResource(R.string.chat_sug_6),
            stringResource(R.string.chat_sug_7),
            stringResource(R.string.chat_sug_8)
        )

        sugerencias.forEach { sugerencia ->
            OutlinedCard(
                onClick = { onSuggestionClick(sugerencia) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Text(
                    text = sugerencia,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LksOrange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(mensaje: Pair<String, Boolean>) {
    val (texto, esUsuario) = mensaje
    val style = getBubbleStyle(esUsuario)

    // Si detecta la clave de error mandada por el ViewModel, usa stringResource en la UI
    val contenidoTexto = when (texto) {
        "ERROR_SYSTEM_INACTIVE" -> stringResource(R.string.system_temporarily_inactive)
        else -> texto
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = style.alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(style.shape)
                .background(style.backgroundColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = style.senderLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = style.senderColor,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = contenidoTexto,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = style.textColor
            )
        }
    }
}

@Composable
private fun ChatLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = LksOrange
                )
                Text(
                    text = stringResource(R.string.chat_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    textoUsuario: String,
    cargando: Boolean,
    onTextoChange: (String) -> Unit,
    onEnviarClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            LksTextField(
                value = textoUsuario,
                onValueChange = onTextoChange,
                label = stringResource(R.string.chat_input_label),
                placeholder = stringResource(R.string.chat_input_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LksButton(
                onClick = onEnviarClick,
                text = stringResource(R.string.chat_input_send),
                enabled = !cargando && textoUsuario.isNotBlank(),
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}

private data class BubbleStyle(
    val alignment: Alignment,
    val backgroundColor: Color,
    val senderColor: Color,
    val textColor: Color,
    val senderLabel: String,
    val shape: RoundedCornerShape
)

@Composable
private fun getBubbleStyle(esUsuario: Boolean): BubbleStyle {
    return if (esUsuario) {
        BubbleStyle(
            alignment = Alignment.CenterEnd,
            backgroundColor = LksOrange,
            senderColor = paleOrange,
            textColor = Color.White,
            senderLabel = stringResource(R.string.chat_sender_you),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        )
    } else {
        BubbleStyle(
            alignment = Alignment.CenterStart,
            backgroundColor = Color.White,
            senderColor = LksOrange,
            textColor = darkBlue,
            senderLabel = stringResource(R.string.chat_sender_bot),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
        )
    }
}