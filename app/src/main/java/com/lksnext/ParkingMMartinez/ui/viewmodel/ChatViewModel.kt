package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    val mensajes = mutableStateListOf<Pair<String, Boolean>>()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    fun resetChat() {
        mensajes.clear()
        _cargando.value = false
    }

    fun enviarPregunta(pregunta: String) {
        if (pregunta.isBlank()) return

        mensajes.add(Pair(pregunta, true))
        _cargando.value = true

        viewModelScope.launch {
            val resultado = repository.sendFAQ(pregunta)

            _cargando.value = false

            resultado.onSuccess { respuestaIA ->
                mensajes.add(Pair(respuestaIA, false))
            }.onFailure { error ->
                mensajes.add(Pair("ERROR_SYSTEM_INACTIVE", false))
                error.printStackTrace()
            }
        }
    }
}