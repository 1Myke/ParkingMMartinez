package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.ZoneNames
import com.lksnext.ParkingMMartinez.model.VehicleType
import java.util.*
import java.text.SimpleDateFormat
import java.time.LocalTime

class BookingViewModel (
    private val repository: BookingRepository,
    private val sessionManager: SessionManager
): ViewModel() {
    var startHour by mutableStateOf(8)
        private set
    var startMinute by mutableStateOf(0)
        private set
    var duration by mutableStateOf(4f)
        private set
    var selectedDay by mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
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

    fun onDateSelected(day: Int) {
        selectedDay = day
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

        editingReservationId?.let { oldId ->
            repository.cancelReservation(oldId)
        }

        val userId = sessionManager.getActiveUserId() ?: ""
        val vehicleWithId = vehicle.copy(id = userId) // Nos aseguramos de que lleve el ID del dueño. MEJORAS: Darle un id unico al vehiculo

        val start = LocalTime.of(startHour, startMinute)
        val end = start.plusHours(duration.toLong())

        val zoneType = when (parkingZone) {
            ZoneNames.DISABILITY -> VehicleType.ADAPTED
            ZoneNames.EV -> VehicleType.ELECTRIC
            ZoneNames.MOTORCYCLE -> VehicleType.MOTORCYCLE
            ZoneNames .STANDARD-> VehicleType.STANDARD
            else -> {
                // Por si llega algo rarete
                android.util.Log.e("MAP_ERROR", "Nombre de zona no reconocido: $parkingZone")
                VehicleType.STANDARD
            }
        }

        val assignedSpot = com.lksnext.ParkingMMartinez.data.ParkingMock.occupyFirstAvailableSpot(zoneType) ?: 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
        // Ponemos a cero las horas en la fecha para que no interfieran con los LocalTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val newReservation = Reservation(
            id = UUID.randomUUID().toString(),
            vehicle = vehicleWithId,
            zone = zone,
            date = calendar.time, // Ahora va una fecha "limpia" de residuos horarios
            startTime = start,
            endTime = end,
            isCheckedIn = false,
            spotNumber = assignedSpot
        )

        repository.saveReservation(newReservation)

        editingReservationId = null
        hasActiveReservation = true
        onComplete()
    }

    fun checkUserReservationStatus() {
        val currentUserId = sessionManager.getActiveUserId()
        if (currentUserId == null) {
            hasActiveReservation = false
            return
        }
        val allBookings = repository.getAllReservations()

        hasActiveReservation = allBookings.any { it.vehicle.id == currentUserId && it.id != editingReservationId }
    }

    fun loadAndFilterVehicles(context: Context) {
        val userId = com.lksnext.ParkingMMartinez.data.SessionManager(context).getActiveUserId() ?: ""
        val allVehicles = com.lksnext.ParkingMMartinez.data.VehicleManager(context).getVehicles(userId)

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
    }

    fun isDateTimeValid(): Boolean {
        val now = Calendar.getInstance()

        // 🎯 MARGEN DE CORTESÍA: Le restamos 5 minutos a la hora actual
        // para que si el usuario reserva en el mismo minuto exacto (o se retrasa unos segundos),
        // el sistema no lo tome como una reserva en el pasado.
        now.add(Calendar.MINUTE, -5)

        val selected = Calendar.getInstance()

        // Configuramos el calendario con lo seleccionado por el usuario
        selected.set(Calendar.DAY_OF_MONTH, selectedDay)
        selected.set(Calendar.HOUR_OF_DAY, startHour)
        selected.set(Calendar.MINUTE, startMinute)
        // Limpiamos segundos y milisegundos para una comparación limpia
        selected.set(Calendar.SECOND, 0)
        selected.set(Calendar.MILLISECOND, 0)

        // Si el momento seleccionado es posterior al "ahora con margen", es válido
        return selected.after(now)
    }

    fun loadReservationForEditing(reservation: Reservation) {
        editingReservationId = reservation.id
        parkingZone = reservation.zone.name
        startHour = reservation.startTime.hour
        startMinute = reservation.startTime.minute
        selectedDay = Calendar.getInstance().apply { time = reservation.date }.get(Calendar.DAY_OF_MONTH)

        // Calculamos la duración más o menos
        val diff = java.time.Duration.between(reservation.startTime, reservation.endTime).toHours()
        duration = diff.toFloat().coerceIn(1f, 8f)
    }

    fun cancelEditing() {
        editingReservationId = null

    }


}