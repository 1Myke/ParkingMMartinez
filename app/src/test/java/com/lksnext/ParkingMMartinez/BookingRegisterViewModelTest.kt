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
import java.time.LocalTime
import java.util.Date

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
        // 🌟 CORREGIDO: Verificamos que ambas listas reales de tu ViewModel queden vacías
        assertTrue(viewModel.activeReservations.isEmpty())
        assertTrue(viewModel.pastReservations.isEmpty())
        verifyNoInteractions(mockRepository)
    }

    @Test
    fun loadReservations_whenUserLogged_populatesState() = runTest {
        // Arrange
        val mockReservation = mock(Reservation::class.java)
        // Simulamos tiempos en el futuro para que la partición lo clasifique en activas
        `when`(mockReservation.date).thenReturn(Date())
        `when`(mockReservation.startTime).thenReturn(LocalTime.now().plusHours(1))
        `when`(mockReservation.endTime).thenReturn(LocalTime.now().plusHours(2))

        val fakeList = listOf(mockReservation)

        // SOLUCCIÓN NATIVA: doAnswer intercepta las funciones suspend de manera segura en Mockito puro
        doAnswer { fakeList }.`when`(mockRepository).getUserReservations(userId)

        // Act
        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle() // Forzamos la ejecución de la corrutina

        // Assert
        // 🌟 CORREGIDO: Comprobamos el tamaño de activeReservations ya que se calcula en el futuro
        assertEquals(1, viewModel.activeReservations.size)
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

        // 🌟 CORREGIDO: Verificamos el estado de tus listas reales tras la cancelación
        assertTrue(viewModel.activeReservations.isEmpty())
        assertTrue(viewModel.pastReservations.isEmpty())
    }
}