package com.lksnext.parkingmmartinez

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.graphics.Color
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
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
import kotlin.collections.emptyList
import com.lksnext.ParkingMMartinez.R

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepository: BookingRepository
    private lateinit var mockVehicleRepository: VehicleRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockContext: Context
    private lateinit var mockNotificationRepository: NotificationRepository
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

    // Controladores de Mocks estáticos y de construcción de Android SDK
    private var mockedPendingIntent: org.mockito.MockedStatic<android.app.PendingIntent>? = null
    private var mockedIntent: org.mockito.MockedConstruction<android.content.Intent>? = null

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock(BookingRepository::class.java)
        mockVehicleRepository = mock(VehicleRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        mockContext = mock(Context::class.java)
        mockNotificationRepository = mock(NotificationRepository::class.java)

        // Mock del servicio de alarmas nativo
        val mockAlarmManager = mock(android.app.AlarmManager::class.java)
        `when`(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager)

        // Sin este mock, context.applicationContext devuelve null y PendingIntent.getBroadcast
        // no es interceptado por el mock estático (any() no captura null), causando NPE en piCancel.cancel()
        `when`(mockContext.applicationContext).thenReturn(mockContext)

        // Interceptamos la creación de cualquier 'new Intent(...)' para que putExtra no rompa el entorno de la JVM
        mockedIntent = mockConstruction(android.content.Intent::class.java) { mock, _ ->
            `when`(mock.putExtra(anyString(), anyString())).thenReturn(mock)
        }

        // Mockeamos estáticamente PendingIntent para evitar el error "not mocked" en CI/CD y local
        mockedPendingIntent = mockStatic(android.app.PendingIntent::class.java)
        val mockPendingIntent = mock(android.app.PendingIntent::class.java)

        // Indicamos que cualquier llamada a getBroadcast devuelva nuestro objeto simulado seguro
        mockedPendingIntent?.`when`<android.app.PendingIntent> {
            android.app.PendingIntent.getBroadcast(
                any(android.content.Context::class.java),
                anyInt(),
                any(android.content.Intent::class.java),
                anyInt()
            )
        }?.thenReturn(mockPendingIntent)

        // Configuraciones y respuestas base por defecto de los componentes
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)
        `when`(mockContext.getString(anyInt())).thenReturn("Mocked String")
        `when`(mockContext.getString(anyInt(), any(), any())).thenReturn("Mocked Body String")

        // Evitamos que las corrutinas del repositorio queden en un limbo asíncrono
        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())

        viewModel = BookingViewModel(mockRepository, mockVehicleRepository, mockSessionManager, mockNotificationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Liberamos los mocks del framework para evitar fugas de memoria entre tests
        mockedPendingIntent?.close()
        mockedIntent?.close()
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
            // Fecha futura y horas fijas: la reserva está garantizada como "no terminada"
            // independientemente de la hora a la que se ejecute el test (evita flakiness
            // por cruce de medianoche en CI/UTC).
            date = GregorianCalendar(2099, Calendar.JANUARY, 1).time,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            isCheckedIn = false
        )

        `when`(mockRepository.getAllReservations()).thenReturn(listOf(matchingReservation))

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

        // Garantizamos que devuelva lista vacía al calcular huecos disponibles
        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())

        viewModel.confirmReservation(mockContext, fakeVehicle, fakeZone) {
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

        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())
        viewModel.loadReservationForEditing(oldReservation)

        viewModel.confirmReservation(mockContext, fakeVehicle, fakeZone) {}
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockRepository).cancelReservation("old_res_id")
        assertTrue(viewModel.hasActiveReservation)
    }

    // ==========================================
    // 4. TESTS DE FILTRADO DE VEHÍCULOS POR ZONA
    // ==========================================

    @Test
    fun loadAndFilterVehicles_whenZoneIsEV_filtersElectricVehicles() = runTest {
        // Configuramos la zona de la pantalla en Eléctricos (EV)
        viewModel.setZone(ZoneNames.EV)

        val electricCar = Vehicle("id1", userId, "Tesla", "1111AAA", VehicleType.ELECTRIC)
        val standardCar = Vehicle("id2", userId, "Golf", "2222BBB", VehicleType.STANDARD)

        // El repositorio devuelve una lista mixta
        `when`(mockVehicleRepository.getVehicles(userId)).thenReturn(listOf(electricCar, standardCar))

        // Ejecutamos la carga
        viewModel.loadAndFilterVehicles()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificamos que solo se ha quedado con el eléctrico y lo ha seleccionado por defecto
        assertEquals(1, viewModel.userVehicles.size)
        assertEquals(VehicleType.ELECTRIC, viewModel.userVehicles[0].type)
        assertEquals(electricCar, viewModel.selectedVehicle)
    }

    @Test
    fun loadAndFilterVehicles_whenRepositoryFails_clearsVehicleListsSilently() = runTest {
        viewModel.setZone(ZoneNames.STANDARD)

        mockStatic(android.util.Log::class.java).use { mockedLog ->
            mockedLog.`when`<Int> {
                android.util.Log.e(anyString(), anyString())
            }.thenReturn(0)

            `when`(mockVehicleRepository.getVehicles(userId))
                .thenThrow(RuntimeException("Firebase connection timeout"))

            viewModel.loadAndFilterVehicles()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.userVehicles.isEmpty())
            assertNull(viewModel.selectedVehicle)
        }
    }

    // ==========================================
    // 5. TESTS DE INTEGRACIÓN DE ALERTAS NATIVAS
    // ==========================================

    @Test
    fun programarAlertasDeReserva_schedulesNativeAndroidAlarmsCorrectly() {
        // Definimos de forma explícita todas las firmas posibles de getString()
        `when`(mockContext.getString(anyInt())).thenReturn("Título de Alerta Fijo")
        `when`(mockContext.getString(anyInt(), any(), any())).thenReturn("Cuerpo de Alerta con parámetros")
        `when`(mockContext.getString(anyInt(), anyString())).thenReturn("Cuerpo Alternativo")

        val futureDate = GregorianCalendar().apply { add(Calendar.DAY_OF_YEAR, 2) }.time

        val testReservation = Reservation(
            id = "alert_test_uuid",
            spotNumber = 1,
            vehicle = fakeVehicle,
            zone = fakeZone,
            date = futureDate,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            isCheckedIn = false // Pasa por los tres flujos obligatorios: inicio, recordatorio de check-in y fin
        )

        val alarmManager = mockContext.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        // Ejecutamos la programación de las 3 alarmas
        viewModel.programarAlertasDeReserva(mockContext, testReservation)

        // 🌟 CORRECCIÓN CRÍTICA: La traza demuestra que interactúa perfectamente 3 veces llamando a setExact().
        // Comprobamos que el AlarmManager nativo de Android recibe la orden de registrar las alarmas en el SO.
        verify(alarmManager, times(3)).setExact(anyInt(), anyLong(), any())
    }


    // ==========================================
    // 6. TEST DE BLOQUEO DE BOTON PARA EVITAR ANTIRESERVA
    // ==========================================
    @Test
    fun confirmReservation_secondCallIsIgnored_whenFirstCallIsProcessing() = runTest {
        // 1. CONFIGURACIÓN: Usamos tus variables de clase existentes (mockRepository, fakeVehicle, fakeZone)
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)
        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())

        // 2. EJECUCIÓN: Simulamos el primer clic en el hilo principal
        viewModel.confirmReservation(mockContext, fakeVehicle, fakeZone) { /* onComplete */ }

        // 🔒 VERIFICACIÓN INMEDIATA: Evaluamos instantáneamente en el microsegundo 0 el estado del candado
        assertTrue(viewModel.isLoading)

        // 3. INTENTO DE REENTRADA (Segundo clic ultra rápido mientras procesa)
        viewModel.confirmReservation(mockContext, fakeVehicle, fakeZone) { /* onComplete */ }

        // Liberamos el dispatcher de pruebas para que terminen de procesarse las corrutinas lanzadas
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. VERIFICACIÓN FINAL: El repositorio solo debió haber sido consultado 1 única vez.
        // Si el candado hubiera fallado, el método confirmReservation habría entrado dos veces y llamado 2 veces a getAllReservations().
        verify(mockRepository, times(1)).getAllReservations()
    }

    // ==========================================
    // 7. TESTS DE PUNTOS CIEGOS SONARQUBE
    // ==========================================

    @Test
    fun inicializarPantalla_whenZoneIsDisability_filtersAdaptedVehicles() = runTest {
        val adaptedCar = Vehicle("id1", userId, "Van", "1111AAA", VehicleType.ADAPTED)
        val standardCar = Vehicle("id2", userId, "Golf", "2222BBB", VehicleType.STANDARD)

        `when`(mockVehicleRepository.getVehicles(userId)).thenReturn(listOf(adaptedCar, standardCar))
        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())

        viewModel.inicializarPantalla(ZoneNames.DISABILITY, 20, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.userVehicles.size)
        assertEquals(VehicleType.ADAPTED, viewModel.userVehicles[0].type)
        assertEquals(ZoneNames.DISABILITY, viewModel.parkingZone)
        assertEquals(20, viewModel.startHour)
        assertEquals(0, viewModel.startMinute)
    }

    @Test
    fun confirmReservation_triggersHalfCapacityBroadcast_whenCapacityReaches50Percent() = runTest {
        val baseDate = GregorianCalendar(2099, Calendar.JANUARY, 1).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        viewModel.onDateSelected(baseDate)
        viewModel.setZone(ZoneNames.EV)
        viewModel.onTimeChange(10, 0)
        viewModel.onDurationChange(2.0f)

        val electricVehicle = Vehicle("ev_1", userId, "Tesla", "1234EV", VehicleType.ELECTRIC)
        val evZone = ParkingZone(ZoneNames.EV, 4, 4, 0, Color(0xFF455A64))

        val existingRes = Reservation(
            id = "res_1",
            spotNumber = 1,
            vehicle = electricVehicle.copy(userId = "other_user"),
            zone = evZone,
            date = baseDate,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            isCheckedIn = false
        )

        `when`(mockRepository.getAllReservations()).thenReturn(listOf(existingRes))
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        var onCompleteCalled = false
        viewModel.confirmReservation(mockContext, electricVehicle, evZone) {
            onCompleteCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(onCompleteCalled)
        
        val expectedDateIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(baseDate)
        verify(mockNotificationRepository).sendBroadcastNotification(
            mockContext,
            R.string.notification_half_capacity_title,
            R.string.notification_half_capacity_body,
            listOf(ZoneNames.EV, expectedDateIso)
        )
    }

    @Test
    fun onReservationCancelled_notifiesSubscribers_whenSpotsBecomeAvailable() = runTest {
        val baseDate = GregorianCalendar(2099, Calendar.JANUARY, 1).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val evZone = ParkingZone(ZoneNames.EV, 4, 4, 0, Color(0xFF455A64))
        val cancelledVehicle = Vehicle("ev_1", userId, "Tesla", "1234EV", VehicleType.ELECTRIC)
        val cancelledRes = Reservation(
            id = "res_1",
            spotNumber = 1,
            vehicle = cancelledVehicle,
            zone = evZone,
            date = baseDate,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            isCheckedIn = false
        )

        `when`(mockRepository.getAllReservations()).thenReturn(emptyList())

        viewModel.onReservationCancelled(mockContext, cancelledRes)
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedDateIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(baseDate)
        verify(mockNotificationRepository).notifyAndClearZoneSubscribers(
            mockContext,
            ZoneNames.EV,
            expectedDateIso,
            R.string.notification_zone_available_title,
            R.string.notification_zone_available_body,
            listOf(ZoneNames.EV, expectedDateIso)
        )
    }

    @Test
    fun performValidationLocally_detectsOverlapConflict_andDisablesButton() = runTest {
        val baseDate = GregorianCalendar(2099, Calendar.JANUARY, 1).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val electricVehicle = Vehicle("ev_1", userId, "Tesla", "1234EV", VehicleType.ELECTRIC)

        val conflictingBookings = (1..4).map {
            Reservation(
                id = "conflicting_$it",
                spotNumber = it,
                vehicle = electricVehicle.copy(userId = "user_$it"),
                zone = ParkingZone(ZoneNames.EV, 4, 4, 0, Color(0xFF455A64)),
                date = baseDate,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(12, 0),
                isCheckedIn = false
            )
        }

        `when`(mockRepository.getAllReservations()).thenReturn(conflictingBookings)
        `when`(mockVehicleRepository.getVehicles(userId)).thenReturn(listOf(electricVehicle))

        viewModel.onDateSelected(baseDate)
        viewModel.inicializarPantalla(ZoneNames.EV, 10, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isOverlapConflict)
        assertFalse(viewModel.isButtonEnabled)
        assertEquals("10:00", viewModel.nextCollisionTime)
    }
}
