package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.ParkingManager
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.ZoneNames
import com.lksnext.ParkingMMartinez.model.VehicleType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class BookingViewModel (
    private val repository: BookingRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionManager: SessionManager
): ViewModel() {
    var startHour by mutableStateOf(8)
        private set
    var startMinute by mutableStateOf(0)
        private set
    var duration by mutableStateOf(4f)
        private set
    var selectedDate by mutableStateOf(Date())
        private set
    var showTimePicker by mutableStateOf(false)
        private set
    var parkingZone by mutableStateOf("Standard Zone")
        private set
    var hasActiveReservation by mutableStateOf(false)
        private set

    var userVehicles by mutableStateOf<List<Vehicle>>(emptyList())
        private set

    var selectedVehicle by mutableStateOf<Vehicle?>(null)

    var editingReservationId by mutableStateOf<String?>(null)
        private set

    var nextCollisionTime by mutableStateOf<String?>(null)
        private set

    var maxAllowedHours by mutableStateOf(0)
        private set

    var isOverlapConflict by mutableStateOf(false)
        private set
    var isButtonEnabled by mutableStateOf(false)
        private set

    // Logica para los proximos 7 dias
    val availableDates: List<Pair<Int, String>> by lazy {
        val calendar = Calendar.getInstance()
        (0..7).map { offset ->
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_YEAR, offset)
            val dayNum = tempCal.get(Calendar.DAY_OF_MONTH)
            val dayName = if (offset == 0) "TODAY"
            else SimpleDateFormat("EEE", Locale.ENGLISH).format(tempCal.time).uppercase()
            dayNum to dayName
        }
    }

    fun onTimeChange(h: Int, m: Int) {
        startHour = h
        startMinute = m
    }

    fun onDurationChange(newDuration: Float) {
        // duration = if (newDuration > 9f) 9f else newDuration
        // Para forzar que nunca pase de 8.0f aunque el slider se mueva raro (Por si acaso)
        duration = newDuration.coerceIn(1.0f, 8.0f)
    }

    fun onDateSelected(date: Date) {
        selectedDate = date
    }

    // Cálculos derivados (Lo que dice el PDF sobre reactividad)
    fun getEndTime(): String {
        // Usamos Calendar para que él haga el trabajo sucio de las horas y minutos
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, startMinute)

        // Sumamos la duración (en horas)
        calendar.add(Calendar.HOUR_OF_DAY, duration.toInt())

        val endHour = calendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = calendar.get(Calendar.MINUTE)

        return String.format("%02d:%02d", endHour, endMinute)
    }

    fun isNextDay(): Boolean {
        return (startHour + duration.toInt()) >= 24
    }

    fun onShowTimePickerChange(show: Boolean) {
        showTimePicker = show
    }

    fun setZone(name: String) {
        parkingZone = name
    }

    fun confirmReservation(
        vehicle: Vehicle,
        zone: ParkingZone,
        onComplete: () -> Unit
    ) {
        val userId = sessionManager.getActiveUserId() ?: ""
        val vehicleWithId = vehicle.copy(id = userId)
        val start = LocalTime.of(startHour, startMinute)
        val end = start.plusHours(duration.toLong())

        val calendar = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        viewModelScope.launch {
            // CORRECCIÓN CRÍTICA: Nos descargamos las reservas actuales para calcular la plaza libre real
            val allBookings = repository.getAllReservations()

            // Calculamos de forma inteligente el primer número de plaza desocupado en ese tramo
            val calculatedSpotNumber = ParkingManager.findFirstAvailableSpotNumber(
                allBookings = allBookings,
                zoneName = parkingZone,
                vehicleType = vehicle.type,
                selectedDate = calendar.time,
                slotStart = start,
                slotEnd = end,
                editingReservationId = editingReservationId
            )

            val newReservation = Reservation(
                id = editingReservationId ?: UUID.randomUUID().toString(),
                vehicle = vehicleWithId,
                zone = zone,
                date = calendar.time,
                startTime = start,
                endTime = end,
                isCheckedIn = false,
                spotNumber = calculatedSpotNumber // <--- ¡Y le asignamos su plaza real! (1, 35, etc.)
            )

            editingReservationId?.let { repository.cancelReservation(it) }
            repository.saveReservation(newReservation)

            editingReservationId = null
            hasActiveReservation = true
            onComplete()
        }
    }
    fun checkUserReservationStatus() {
        val currentUserId = sessionManager.getActiveUserId() ?: return

        viewModelScope.launch {
            val allBookings = repository.getAllReservations()
            val nowMillis = System.currentTimeMillis()

            // 🌟 CORRECCIÓN: Solo bloquea si la reserva acaba en el futuro
            hasActiveReservation = allBookings.any { booking ->
                booking.vehicle.userId == currentUserId && !isReservationPast(booking, nowMillis)
            }
        }
    }
    private fun isReservationPast(res: Reservation, nowMillis: Long): Boolean {
        val endCal = Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.endTime.hour)
            set(Calendar.MINUTE, res.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora de fin es menor a la de inicio, cruzó la medianoche
            if (res.endTime.hour < res.startTime.hour) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return endCal.timeInMillis <= nowMillis
    }

    fun loadAndFilterVehicles() {
        val userId = sessionManager.getActiveUserId() ?: ""

        viewModelScope.launch {
            try {
                val allVehicles = vehicleRepository.getVehicles(userId)

                val requiredType = when (parkingZone) {
                    "Disability", ZoneNames.DISABILITY -> VehicleType.ADAPTED
                    "EV", ZoneNames.EV -> VehicleType.ELECTRIC
                    "Motorcycle", ZoneNames.MOTORCYCLE -> VehicleType.MOTORCYCLE
                    "Standard", ZoneNames.STANDARD -> VehicleType.STANDARD
                    else -> VehicleType.STANDARD
                }

                val filtered = allVehicles.filter { it.type == requiredType }

                userVehicles = filtered
                selectedVehicle = if (filtered.isNotEmpty()) filtered[0] else null

                validateBooking()

            } catch (e: Exception) {
                android.util.Log.e("FIREBASE_ERROR", "Error cargando vehículos en la reserva: ${e.message}")
                userVehicles = emptyList()
                selectedVehicle = null
            }
        }
    }

    fun isDateTimeValid(): Boolean {
        val now = Calendar.getInstance()
        now.add(Calendar.MINUTE, -5)

        val selected = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return selected.after(now)
    }

    fun loadReservationForEditing(reservation: Reservation) {
        editingReservationId = reservation.id
        parkingZone = reservation.zone.name
        startHour = reservation.startTime.hour
        startMinute = reservation.startTime.minute
        selectedDate = reservation.date
        // Calculamos la duración más o menos
        val diff = java.time.Duration.between(reservation.startTime, reservation.endTime).toHours()
        duration = diff.toFloat().coerceIn(1f, 8f)
    }

    fun cancelEditing() {
        editingReservationId = null

    }

    fun validateBooking() {
        viewModelScope.launch {
            isButtonEnabled = performValidation()
        }
    }

    private suspend fun performValidation(): Boolean {
        isOverlapConflict = false
        nextCollisionTime = null
        maxAllowedHours = 0

        if (!isDateTimeValid() || selectedVehicle == null) return false
        if (editingReservationId != null) return true

        val currentUserId = sessionManager.getActiveUserId() ?: return false
        val allBookings = repository.getAllReservations()
        val nowMillis = System.currentTimeMillis()

        // 🌟 CORRECCIÓN CRÍTICA: Ignoramos las reservas del usuario que ya pertenecen al pasado
        val hasRealActive = allBookings.any { booking ->
            booking.vehicle.userId == currentUserId && !isReservationPast(booking, nowMillis)
        }
        if (hasRealActive) return false

        val proposedStart = LocalTime.of(startHour, startMinute)
        val proposedEnd = proposedStart.plusHours(duration.toLong())
        val mockZone = com.lksnext.ParkingMMartinez.data.ParkingManager.zones.find { it.name == parkingZone }
        val maxSpotsInZone = mockZone?.totalSpots ?: 4

        val conflictingBookings = allBookings.filter { booking ->
            val calProposed = Calendar.getInstance().apply { time = selectedDate }
            val calBooking = Calendar.getInstance().apply { time = booking.date }

            calProposed.get(Calendar.YEAR) == calBooking.get(Calendar.YEAR) &&
                    calProposed.get(Calendar.DAY_OF_YEAR) == calBooking.get(Calendar.DAY_OF_YEAR) &&
                    booking.zone.name == parkingZone
        }

        var tempTime = proposedStart
        var earliestCollision: LocalTime? = null
        while (tempTime.isBefore(proposedEnd)) {
            val carsAtThisHour = conflictingBookings.count { b ->
                val startsBeforeOrAt = b.startTime.isBefore(tempTime) || b.startTime == tempTime
                val endsAfter = b.endTime.isAfter(tempTime)
                startsBeforeOrAt && endsAfter
            }
            if (carsAtThisHour >= maxSpotsInZone && earliestCollision == null) earliestCollision = tempTime
            tempTime = tempTime.plusMinutes(30)
        }

        if (earliestCollision != null) {
            isOverlapConflict = true
            nextCollisionTime = String.format("%02d:%02d", earliestCollision.hour, earliestCollision.minute)
            maxAllowedHours = (java.time.Duration.between(proposedStart, earliestCollision).toMinutes() / 60).toInt()
            return false
        }
        return true
    }

}