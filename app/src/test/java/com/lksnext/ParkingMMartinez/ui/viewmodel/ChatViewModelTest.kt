package com.lksnext.ParkingMMartinez.ui.viewmodel

import com.lksnext.ParkingMMartinez.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    private lateinit var viewModel: ChatViewModel
    private val repository: ChatRepository = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enviarPregunta_adds_user_message_and_bot_response_on_success() = runTest {
        val question = "Hola"
        val answer = "Hola, en qué te ayudo"
        whenever(repository.sendFAQ(question)).thenReturn(Result.success(answer))

        viewModel.enviarPregunta(question)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.mensajes.size)
        assertEquals(Pair(question, true), viewModel.mensajes[0])
        assertEquals(Pair(answer, false), viewModel.mensajes[1])
        assertEquals(false, viewModel.cargando.value)
    }

    @Test
    fun enviarPregunta_does_nothing_on_blank_question() = runTest {
        viewModel.enviarPregunta(" ")
        testScheduler.advanceUntilIdle()

        assertEquals(0, viewModel.mensajes.size)
    }

    @Test
    fun enviarPregunta_adds_fallback_on_failure() = runTest {
        val question = "Hola"
        whenever(repository.sendFAQ(question)).thenReturn(Result.failure(Exception("Error")))

        viewModel.enviarPregunta(question)
        testScheduler.advanceUntilIdle()

        assertEquals(2, viewModel.mensajes.size)
        assertEquals(Pair(question, true), viewModel.mensajes[0])
        assertEquals(Pair("ERROR_SYSTEM_INACTIVE", false), viewModel.mensajes[1])
        assertEquals(false, viewModel.cargando.value)
    }

    @Test
    fun resetChat_clears_messages() {
        viewModel.mensajes.add(Pair("Test", true))
        viewModel.resetChat()
        assertEquals(0, viewModel.mensajes.size)
        assertEquals(false, viewModel.cargando.value)
    }
}

