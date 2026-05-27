package com.lksnext.ParkingMMartinez

import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingRegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class BookingRegisterViewModelTest {

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: BookingRegisterViewModel

    private val userId = "mikel_123"

    @Before
    fun setUp() {
        mockRepository = mock(BookingRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        // Configuramos sesión activa por defecto
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel = BookingRegisterViewModel(mockRepository, mockSessionManager)
    }

    @Test
    fun loadReservations_whenUserNotLogged_clearsReservations() {
        // Arrange
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        // Act
        viewModel.loadReservations()

        // Assert
        assertTrue(viewModel.reservations.isEmpty())
        verifyNoInteractions(mockRepository)
    }

    @Test
    fun loadReservations_whenUserLogged_populatesState() {
        // Arrange
        val fakeList = listOf(mock(Reservation::class.java))
        `when`(mockRepository.getUserReservations(userId)).thenReturn(fakeList)

        // Act
        viewModel.loadReservations()

        // Assert
        assertEquals(1, viewModel.reservations.size)
        verify(mockRepository).getUserReservations(userId)
    }

    @Test
    fun cancelReservation_triggersRepositoryAndRefreshesList() {
        // Arrange
        val resId = "res_xyz"
        `when`(mockRepository.getUserReservations(userId)).thenReturn(emptyList())

        // Act
        viewModel.cancelReservation(resId)

        // Assert
        verify(mockRepository).cancelReservation(resId)
        // También verifica que se volvió a pedir la lista actualizada
        verify(mockRepository).getUserReservations(userId)
        assertTrue(viewModel.reservations.isEmpty())
    }
}