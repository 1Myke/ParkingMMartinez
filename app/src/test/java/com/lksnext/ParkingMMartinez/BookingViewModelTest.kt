package com.lksnext.parkingmmartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository // Asegúrate de que el import sea correcto
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import com.lksnext.ParkingMMartinez.ui.viewmodel.BookingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockVehicleRepository: VehicleRepository // CORRECCIÓN: Añadido repositorio faltante
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: BookingViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val userId = "mikel_user_123"

    private val fakeVehicle = Vehicle(
        id = "car_abc",
        userId = userId,
        name = "Golf",
        plate = "1234XYZ",
        type = VehicleType.STANDARD
    )
    private val fakeZone = ParkingZone(ZoneNames.STANDARD, 24, 24, 0, Color(0xFF455A64))

    @Before
    fun setUp() = runTest { // CORRECCIÓN: runTest aquí permite inicializar stubs asíncronos de forma segura
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock(BookingRepository::class.java)
        mockVehicleRepository = mock(VehicleRepository::class.java) // Inicializamos el mock
        mockSessionManager = mock(SessionManager::class.java)

        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        // CORRECCIÓN: Estructura nativa segura para la función suspendida
        doAnswer { emptyList<Reservation>() }.`when`(mockRepository).getAllReservations()

        // CORRECCIÓN: Pasamos los 3 parámetros en el orden exacto que espera tu ViewModel real
        viewModel = BookingViewModel(mockRepository, mockVehicleRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
    fun onDateSelected_updatesSelectedDate() {
        val targetDate = Date()
        viewModel.onDateSelected(targetDate)
        assertEquals(targetDate, viewModel.selectedDate)
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

        viewModel.onDateSelected(calendar.time)
        viewModel.onTimeChange(12, 0)

        assertTrue(viewModel.isDateTimeValid())
    }

    // ==========================================
    // 3. TESTS DE LÓGICA DE NEGOCIO AVANZADA
    // ==========================================

    @Test
    fun checkUserReservationStatus_whenNoActiveUser_returnsFalse() = runTest {
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        viewModel.checkUserReservationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.hasActiveReservation)
    }

    @Test
    fun checkUserReservationStatus_whenUserHasMatchingReservation_returnsTrue() = runTest {
        val matchingVehicle = Vehicle(
            id = userId,
            userId = userId,
            name = "Golf",
            plate = "1234XYZ",
            type = VehicleType.STANDARD
        )

        val matchingReservation = Reservation(
            id = "res_111",
            spotNumber = 5,
            vehicle = matchingVehicle,
            zone = fakeZone,
            date = Date(),
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusHours(2),
            isCheckedIn = false
        )

        doAnswer { listOf(matchingReservation) }.`when`(mockRepository).getAllReservations()

        viewModel.checkUserReservationStatus()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.hasActiveReservation)
    }

    @Test
    fun loadReservationForEditing_populatesViewModelStates() {
        val targetDate = GregorianCalendar(2026, Calendar.OCTOBER, 10).time
        val targetReservation = Reservation(
            id = "edit_id_999",
            spotNumber = 12,
            vehicle = fakeVehicle,
            zone = fakeZone,
            date = targetDate,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(13, 0),
            isCheckedIn = false
        )

        viewModel.loadReservationForEditing(targetReservation)

        assertEquals("edit_id_999", viewModel.editingReservationId)
        assertEquals(ZoneNames.STANDARD, viewModel.parkingZone)
        assertEquals(9, viewModel.startHour)
        assertEquals(0, viewModel.startMinute)
        assertEquals(targetDate, viewModel.selectedDate)
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
    fun confirmReservation_createsAndSavesNewReservation() = runTest {
        var onCompleteCalled = false
        viewModel.setZone(ZoneNames.STANDARD)
        viewModel.onTimeChange(10, 0)
        viewModel.onDurationChange(3.0f)

        viewModel.confirmReservation(fakeVehicle, fakeZone) {
            onCompleteCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.hasActiveReservation)
        assertNull(viewModel.editingReservationId)
        assertTrue(onCompleteCalled)
    }

    @Test
    fun confirmReservation_whenEditing_cancelsOldReservationBeforeSaving() = runTest {
        val oldReservation = Reservation(
            id = "old_res_id", spotNumber = 12, vehicle = fakeVehicle, zone = fakeZone,
            date = Date(), startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), isCheckedIn = false
        )
        viewModel.loadReservationForEditing(oldReservation)

        viewModel.confirmReservation(fakeVehicle, fakeZone) {}
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockRepository).cancelReservation("old_res_id")
        assertTrue(viewModel.hasActiveReservation)
    }
}