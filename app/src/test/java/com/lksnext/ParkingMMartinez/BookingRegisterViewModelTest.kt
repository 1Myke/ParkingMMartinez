package com.lksnext.ParkingMMartinez

import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class BookingRegisterViewModelTest {

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: BookingRegisterViewModel

    private val userId = "mikel_123"
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Configuramos el despachador Main para entornos de test (necesario para viewModelScope)
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock(BookingRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        // Configuramos sesión activa por defecto
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel = BookingRegisterViewModel(mockRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        // Limpiamos el despachador al terminar cada test
        Dispatchers.resetMain()
    }

    @Test
    fun loadReservations_whenUserNotLogged_clearsReservations() = runTest {
        // Arrange
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        // Act
        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle() // Asegura que la corrutina interna termine

        // Assert
        assertTrue(viewModel.reservations.isEmpty())
        verifyNoInteractions(mockRepository)
    }

    @Test
    fun loadReservations_whenUserLogged_populatesState() = runTest {
        // Arrange
        val fakeList = listOf(mock(Reservation::class.java))

        // SOLUCCIÓN NATIVA: doAnswer intercepta las funciones suspend de manera segura en Mockito puro
        doAnswer { fakeList }.`when`(mockRepository).getUserReservations(userId)

        // Act
        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle() // Forzamos la ejecución de la corrutina

        // Assert
        assertEquals(1, viewModel.reservations.size)
        verify(mockRepository).getUserReservations(userId)
    }

    @Test
    fun cancelReservation_triggersRepositoryAndRefreshesList() = runTest {
        // Arrange
        val resId = "res_xyz"
        doAnswer { emptyList<Reservation>() }.`when`(mockRepository).getUserReservations(userId)

        // Act
        viewModel.cancelReservation(resId)
        testDispatcher.scheduler.advanceUntilIdle() // Forzamos la ejecución de la corrutina

        // Assert
        verify(mockRepository).cancelReservation(resId)
        // También verifica que se volvió a pedir la lista actualizada
        verify(mockRepository).getUserReservations(userId)
        assertTrue(viewModel.reservations.isEmpty())
    }
}