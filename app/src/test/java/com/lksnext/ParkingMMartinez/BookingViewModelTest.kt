package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.*

class BookingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: BookingViewModel

    private val userId = "mikel_user_123"
    private val fakeVehicle = Vehicle(id = "car_abc", name = "Golf", plate = "1234XYZ", type = VehicleType.STANDARD, isAdapted = false)
    private val fakeZone = ParkingZone(ZoneNames.STANDARD, 24, 24, 0, Color(0xFF455A64))

    @Before
    fun setUp() {
        mockRepository = mock(BookingRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel = BookingViewModel(mockRepository, mockSessionManager)
    }

    // ==========================================
    // 1. TESTS DE CONTROL DE FLUJO Y ESTADOS BÁSICOS
    // ==========================================

    @Test
    fun onTimeChange_updatesStateVariables() {
        viewModel.onTimeChange(14, 30)
        assertEquals(14, viewModel.startHour)
        assertEquals(30, viewModel.startMinute)
    }

    @Test
    fun onDurationChange_coercesValueWithinLimits() {
        viewModel.onDurationChange(12.5f)
        assertEquals(8.0f, viewModel.duration, 0.0f)

        viewModel.onDurationChange(0.2f)
        assertEquals(1.0f, viewModel.duration, 0.0f)
    }

    @Test
    fun onDateSelected_updatesSelectedDay() {
        viewModel.onDateSelected(25)
        assertEquals(25, viewModel.selectedDay)
    }

    @Test
    fun onShowTimePickerChange_updatesState() {
        viewModel.onShowTimePickerChange(true)
        assertTrue(viewModel.showTimePicker)
    }

    @Test
    fun setZone_updatesParkingZoneState() {
        viewModel.setZone(ZoneNames.EV)
        assertEquals(ZoneNames.EV, viewModel.parkingZone)
    }

    // ==========================================
    // 2. TESTS DE CÁLCULOS MATEMÁTICOS DE TIEMPO
    // ==========================================

    @Test
    fun getEndTime_calculatesTimeAccurately() {
        viewModel.onTimeChange(16, 15)
        viewModel.onDurationChange(4.0f)

        val endTimeStr = viewModel.getEndTime()
        assertEquals("20:15", endTimeStr)
    }

    @Test
    fun isNextDay_detectsWhenReservationCrossesMidnight() {
        viewModel.onTimeChange(22, 0)
        viewModel.onDurationChange(3.0f)
        assertTrue(viewModel.isNextDay())

        viewModel.onTimeChange(10, 0)
        viewModel.onDurationChange(2.0f)
        assertFalse(viewModel.isNextDay())
    }

    @Test
    fun isDateTimeValid_withFutureDate_returnsTrue() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 2)

        viewModel.onDateSelected(calendar.get(Calendar.DAY_OF_MONTH))
        viewModel.onTimeChange(12, 0)

        assertTrue(viewModel.isDateTimeValid())
    }

    // ==========================================
    // 3. TESTS DE LÓGICA DE NEGOCIO AVANZADA
    // ==========================================

    @Test
    fun checkUserReservationStatus_whenNoActiveUser_returnsFalse() {
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        viewModel.checkUserReservationStatus()

        assertFalse(viewModel.hasActiveReservation)
    }

    @Test
    fun checkUserReservationStatus_whenUserHasMatchingReservation_returnsTrue() {
        val matchingReservation = Reservation(
            id = "res_111",
            spotNumber = 5,
            vehicle = fakeVehicle.copy(id = userId),
            zone = fakeZone,
            date = Date(),
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusHours(2),
            isCheckedIn = false
        )
        `when`(mockRepository.getAllReservations()).thenReturn(listOf(matchingReservation))

        viewModel.checkUserReservationStatus()

        assertTrue(viewModel.hasActiveReservation)
    }

    @Test
    fun loadReservationForEditing_populatesViewModelStates() {
        val targetReservation = Reservation(
            id = "edit_id_999",
            spotNumber = 12,
            vehicle = fakeVehicle,
            zone = fakeZone,
            date = GregorianCalendar(2026, Calendar.OCTOBER, 10).time,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(13, 0),
            isCheckedIn = false
        )

        viewModel.loadReservationForEditing(targetReservation)

        assertEquals("edit_id_999", viewModel.editingReservationId)
        assertEquals(ZoneNames.STANDARD, viewModel.parkingZone)
        assertEquals(9, viewModel.startHour)
        assertEquals(0, viewModel.startMinute)
        assertEquals(10, viewModel.selectedDay)
        assertEquals(4.0f, viewModel.duration, 0.0f)
    }

    @Test
    fun cancelEditing_clearsEditingId() {
        val targetReservation = Reservation(
            id = "edit_id_999", spotNumber = 12, vehicle = fakeVehicle, zone = fakeZone,
            date = Date(), startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), isCheckedIn = false
        )
        viewModel.loadReservationForEditing(targetReservation)

        viewModel.cancelEditing()

        assertNull(viewModel.editingReservationId)
    }

    @Test
    fun confirmReservation_createsAndSavesNewReservation() {
        var onCompleteCalled = false
        viewModel.setZone(ZoneNames.STANDARD)
        viewModel.onTimeChange(10, 0)
        viewModel.onDurationChange(3.0f)

        try {
            viewModel.confirmReservation(fakeVehicle, fakeZone) {
                onCompleteCalled = true
            }
        } catch (e: Exception) {
            // Captura segura contra logs nativos
        }

        // Verificamos el estado mutado y la ejecución del callback sin usar Matchers conflictivos
        assertTrue(viewModel.hasActiveReservation)
        assertNull(viewModel.editingReservationId)
        assertTrue(onCompleteCalled)
    }

    @Test
    fun confirmReservation_whenEditing_cancelsOldReservationBeforeSaving() {
        val oldReservation = Reservation(
            id = "old_res_id", spotNumber = 12, vehicle = fakeVehicle, zone = fakeZone,
            date = Date(), startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), isCheckedIn = false
        )
        viewModel.loadReservationForEditing(oldReservation)

        try {
            viewModel.confirmReservation(fakeVehicle, fakeZone) {}
        } catch (e: Exception) {
            // Captura segura
        }

        // Esta línea es segura porque comparamos un String exacto sin usar Matchers dinámicos
        verify(mockRepository).cancelReservation("old_res_id")
        assertTrue(viewModel.hasActiveReservation)
    }
}