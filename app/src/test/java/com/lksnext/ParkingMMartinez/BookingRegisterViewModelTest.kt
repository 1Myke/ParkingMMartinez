package com.lksnext.ParkingMMartinez

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class BookingRegisterViewModelTest {

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockContext: Context
    private lateinit var mockAlarmManager: AlarmManager
    private lateinit var viewModel: BookingRegisterViewModel

    private val userId = "mikel_123"
    private val testDispatcher = StandardTestDispatcher()
    private var mockedPendingIntent: org.mockito.MockedStatic<PendingIntent>? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock(BookingRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        mockContext = mock(Context::class.java)
        mockAlarmManager = mock(AlarmManager::class.java)

        `when`(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager)

        // Sin este mock, context.applicationContext devuelve null y PendingIntent.getBroadcast
        // no es interceptado por el mock estático, causando NPE en piCancel.cancel() dentro
        // de cancelarAlarmaExistente, lo que interrumpe la corrutina antes de llamar a loadReservations()
        `when`(mockContext.applicationContext).thenReturn(mockContext)

        // Mockeamos estáticamente PendingIntent para evitar el error "Method getBroadcast not mocked"
        mockedPendingIntent = mockStatic(PendingIntent::class.java)
        val mockPendingIntent = mock(PendingIntent::class.java)

        mockedPendingIntent?.`when`<PendingIntent> {
            PendingIntent.getBroadcast(
                any(Context::class.java),
                anyInt(),
                any(Intent::class.java),
                anyInt()
            )
        }?.thenReturn(mockPendingIntent)

        // Configuramos sesión activa por defecto
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel = BookingRegisterViewModel(mockRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedPendingIntent?.close()
    }

    // ==========================================
    // 1. TESTS DE CARGA Y FILTRADO DE RESERVAS (LOS ORIGINALES)
    // ==========================================

    @Test
    fun loadReservations_whenUserNotLogged_clearsReservations() = runTest {
        // Arrange
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        // Act
        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.activeReservations.isEmpty())
        assertTrue(viewModel.pastReservations.isEmpty())
        verifyNoInteractions(mockRepository)
    }

    @Test
    fun loadReservations_whenUserLogged_populatesState() = runTest {
        // Arrange
        val mockReservation = mock(Reservation::class.java)

        // Sincronización temporal perfecta: Mañana para asegurar que caiga siempre en Activas
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        `when`(mockReservation.date).thenReturn(tomorrow)
        `when`(mockReservation.startTime).thenReturn(LocalTime.of(12, 0))
        `when`(mockReservation.endTime).thenReturn(LocalTime.of(14, 0))

        val fakeList = listOf(mockReservation)
        `when`(mockRepository.getUserReservations(userId)).thenReturn(fakeList)

        // Act
        viewModel.loadReservations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(1, viewModel.activeReservations.size)
        verify(mockRepository).getUserReservations(userId)
    }

    @Test
    fun cancelReservation_triggersRepositoryAndRefreshesList() = runTest {
        // Arrange
        val resId = "res_xyz"
        `when`(mockRepository.getUserReservations(userId)).thenReturn(emptyList())

        // Act 1: Simulamos que el usuario le da al botón de cancelar en la tarjeta
        viewModel.askCancelReservation(resId)

        // Comprobamos que el diálogo se abre y guarda el ID correctamente
        assertTrue(viewModel.showCancelConfirmation)
        assertEquals(resId, viewModel.reservationToCancel)

        // Act 2: Simulamos que el usuario confirma en el diálogo
        viewModel.confirmCancelReservation(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(mockRepository).cancelReservation(resId)
        verify(mockRepository).getUserReservations(userId)
        assertTrue(viewModel.activeReservations.isEmpty())
        assertTrue(viewModel.pastReservations.isEmpty())

        // Comprobamos que el diálogo se ha cerrado tras borrar
        assertFalse(viewModel.showCancelConfirmation)
    }

    // ==========================================
    // 2. NUEVOS TESTS ADICIONALES (CORREGIDOS PARA KOTLIN)
    // ==========================================

    @Test
    fun isCheckInWindowActive_whenTimeIsOutsideWindow_returnsFalse() {
        // Arrange: Reserva en el futuro lejano (dentro de 5 días) para estar fuera de rango
        val futureDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 5) }.time
        val futureReservation = mock(Reservation::class.java).apply {
            `when`(date).thenReturn(futureDate)
            `when`(startTime).thenReturn(LocalTime.of(12, 0))
            `when`(endTime).thenReturn(LocalTime.of(14, 0))
        }

        // Act
        val isActive = viewModel.isCheckInWindowActive(futureReservation)

        // Assert
        assertFalse(isActive)
    }

    @Test
    fun isCheckInWindowActive_whenTimeIsExactlyInside_returnsTrue() {
        // Arrange: Sincronizamos la reserva exactamente con el reloj actual del sistema
        val nowCal = Calendar.getInstance()
        val currentLocalTime = LocalTime.of(nowCal.get(Calendar.HOUR_OF_DAY), nowCal.get(Calendar.MINUTE))

        val liveReservation = mock(Reservation::class.java).apply {
            `when`(date).thenReturn(nowCal.time)
            `when`(startTime).thenReturn(currentLocalTime)
            `when`(endTime).thenReturn(currentLocalTime.plusHours(1))
        }

        // Act
        val isActive = viewModel.isCheckInWindowActive(liveReservation)

        // Assert
        assertTrue(isActive)
    }

    @Test
    fun doCheckIn_updatesReservationToTrueAndRefreshes() = runTest {
        // Arrange
        val testReservation = Reservation(
            id = "res_checkin_123",
            spotNumber = 4,
            vehicle = mock(com.lksnext.ParkingMMartinez.model.Vehicle::class.java),
            zone = mock(com.lksnext.ParkingMMartinez.model.ParkingZone::class.java),
            date = Date(),
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusHours(1),
            isCheckedIn = false
        )
        `when`(mockRepository.getUserReservations(userId)).thenReturn(emptyList())

        // Act
        viewModel.doCheckIn(testReservation)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert: 🌟 El truco Elvis (?: testReservation) evita el NPE protegiendo la firma de tipos de Kotlin
        val captor = ArgumentCaptor.forClass(Reservation::class.java)
        verify(mockRepository).saveReservation(captor.capture() ?: testReservation)

        // Evaluamos el objeto capturado de forma 100% segura
        assertEquals("res_checkin_123", captor.value.id)
        assertTrue(captor.value.isCheckedIn)

        // Comprobamos que refresca la UI pidiendo de nuevo las reservas
        verify(mockRepository).getUserReservations(userId)
    }
}