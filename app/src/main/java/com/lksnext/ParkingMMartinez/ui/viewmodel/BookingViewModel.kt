package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.data.ParkingManager
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.receiver.BookingAlarmReceiver
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TIME_FORMAT = "%02d:%02d"
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

    var parkingZone by mutableStateOf(ZoneNames.STANDARD)
        private set
    var hasActiveReservation by mutableStateOf(false)
        private set

    var userVehicles by mutableStateOf<List<Vehicle>>(emptyList())
        private set

    var selectedVehicle by mutableStateOf<Vehicle?>(null)

    var editingReservationId by mutableStateOf<String?>(null)
        private set

    var isEditingCheckedIn by mutableStateOf(false)
        private set

    var nextCollisionTime by mutableStateOf<String?>(null)
        private set

    var maxAllowedHours by mutableStateOf(0)
        private set

    var isOverlapConflict by mutableStateOf(false)
        private set
    var isButtonEnabled by mutableStateOf(false)
        private set

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
        duration = newDuration.coerceIn(1.0f, 8.0f)
    }

    fun onDateSelected(date: Date) {
        selectedDate = date
    }

    fun getEndTime(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, startMinute)
        calendar.add(Calendar.HOUR_OF_DAY, duration.toInt())

        val endHour = calendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = calendar.get(Calendar.MINUTE)

        return String.format(Locale.getDefault(), TIME_FORMAT, endHour, endMinute)
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
        context: Context,
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
            val allBookings = repository.getAllReservations()

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
                isCheckedIn = isEditingCheckedIn,
                spotNumber = calculatedSpotNumber
            )

            editingReservationId?.let { repository.cancelReservation(it) }
            repository.saveReservation(newReservation)

            programarAlertasDeReserva(context, newReservation)

            editingReservationId = null
            isEditingCheckedIn = false
            hasActiveReservation = true
            onComplete()
        }
    }

    fun checkUserReservationStatus() {
        val currentUserId = sessionManager.getActiveUserId() ?: return

        viewModelScope.launch {
            val allBookings = repository.getAllReservations()
            val nowMillis = System.currentTimeMillis()

            hasActiveReservation = allBookings.any { booking ->
                val isUserReservation = booking.vehicle.userId == currentUserId


                val isTotallyFinished = isReservationPastEntirely(booking, nowMillis)

                isUserReservation && !isTotallyFinished
            }
        }
    }

    private fun isReservationPastEntirely(res: Reservation, nowMillis: Long): Boolean {
        val endCal = Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.endTime.hour)
            set(Calendar.MINUTE, res.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return endCal.timeInMillis <= nowMillis
    }

    private fun isReservationPast(res: Reservation, nowMillis: Long): Boolean {
        val endCal = Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.endTime.hour)
            set(Calendar.MINUTE, res.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

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
        if (editingReservationId != null && isEditingCheckedIn) {
            return true
        }

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
        isEditingCheckedIn = reservation.isCheckedIn
        parkingZone = reservation.zone.name
        startHour = reservation.startTime.hour
        startMinute = reservation.startTime.minute
        selectedDate = reservation.date
        val diff = java.time.Duration.between(reservation.startTime, reservation.endTime).toHours()
        duration = diff.toFloat().coerceIn(1f, 8f)
    }

    fun cancelEditing() {
        editingReservationId = null
        isEditingCheckedIn = false
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

        val hasRealActive = allBookings.any { booking ->
            val isUserReservation = booking.vehicle.userId == currentUserId
            val isTotallyFinished = isReservationPastEntirely(booking, nowMillis)

            isUserReservation && !isTotallyFinished
        }
        if (hasRealActive) return false

        val proposedStart = LocalTime.of(startHour, startMinute)
        val proposedEnd = proposedStart.plusHours(duration.toLong())
        val mockZone = ParkingManager.zones.find { it.name == parkingZone }
        val maxSpotsInZone = mockZone?.totalSpots ?: 4

        val conflictingBookings = allBookings.filter { booking ->
            val calProposed = Calendar.getInstance().apply { time = selectedDate }
            val calBooking = Calendar.getInstance().apply { time = booking.date }
            val isConflictingUserPresent = !isReservationPast(booking, nowMillis) || booking.isCheckedIn

            calProposed.get(Calendar.YEAR) == calBooking.get(Calendar.YEAR) &&
                    calProposed.get(Calendar.DAY_OF_YEAR) == calBooking.get(Calendar.DAY_OF_YEAR) &&
                    booking.zone.name == parkingZone &&
                    isConflictingUserPresent
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
            nextCollisionTime = String.format(Locale.getDefault(), TIME_FORMAT, earliestCollision.hour, earliestCollision.minute)
            maxAllowedHours = (java.time.Duration.between(proposedStart, earliestCollision).toMinutes() / 60).toInt()
            return false
        }
        return true
    }

    fun programarAlertasDeReserva(context: Context, reservation: Reservation) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val idAlertaInicio = reservation.id.hashCode() + 1
        val idAlertaFin = reservation.id.hashCode() + 2
        val idAlertaCheckIn = reservation.id.hashCode() + 3

        // --- CANCELAR ALARMAS VIEJAS POR SI ES UNA EDICIÓN ---
        cancelarAlarmaExistente(context, alarmManager, idAlertaInicio)
        cancelarAlarmaExistente(context, alarmManager, idAlertaFin)
        cancelarAlarmaExistente(context, alarmManager, idAlertaCheckIn)

        val nowMillis = System.currentTimeMillis()

        // --- 1. CONFIGURAR NUEVA ALERTA DE INICIO (15 minutos antes) ---
        val calInicio = Calendar.getInstance().apply {
            time = reservation.date
            set(Calendar.HOUR_OF_DAY, reservation.startTime.hour)
            set(Calendar.MINUTE, reservation.startTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -15)
        }

        if (calInicio.timeInMillis > System.currentTimeMillis()) {
            val tituloInicio = context.getString(R.string.notification_title_start)
            val horaFormateada = String.format(Locale.getDefault(), TIME_FORMAT, reservation.startTime.hour, reservation.startTime.minute)
            val cuerpoInicio = context.getString(R.string.notification_body_start, reservation.zone.name, horaFormateada)

            configurarAlertaNativa(context, alarmManager, calInicio.timeInMillis, idAlertaInicio, tituloInicio, cuerpoInicio)
        }

        // --- 2. CONFIGURAR NUEVA ALERTA DE RECORDATORIO CHECK-IN (+15 minutos de la hora de inicio) ---
        if (!reservation.isCheckedIn) {
            val calCheckIn = Calendar.getInstance().apply {
                time = reservation.date
                set(Calendar.HOUR_OF_DAY, reservation.startTime.hour)
                set(Calendar.MINUTE, reservation.startTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, 15) // 15 minutos (ahora mismo esta en 1 minuto)
            }

            if (calCheckIn.timeInMillis > nowMillis) {
                val tituloCheckIn = context.getString(R.string.notification_title_checkin_reminder)
                val cuerpoCheckIn = context.getString(R.string.notification_body_checkin_reminder, reservation.zone.name)

                configurarAlertaNativa(context, alarmManager, calCheckIn.timeInMillis, idAlertaCheckIn, tituloCheckIn, cuerpoCheckIn)
            }
        }

        // --- 3. CONFIGURAR NUEVA ALERTA DE FIN (15 minutos antes de salir) ---
        val calFin = Calendar.getInstance().apply {
            time = reservation.date
            set(Calendar.HOUR_OF_DAY, reservation.endTime.hour)
            set(Calendar.MINUTE, reservation.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (reservation.endTime.hour < reservation.startTime.hour) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            add(Calendar.MINUTE, -15)
        }

        if (calFin.timeInMillis > nowMillis) {
            val tituloFin = context.getString(R.string.notification_title_end)
            val cuerpoFin = context.getString(R.string.notification_body_end)

            configurarAlertaNativa(context, alarmManager, calFin.timeInMillis, idAlertaFin, tituloFin, cuerpoFin)
        }
    }

    // Funcion para configurar la alerta nativa
    private fun configurarAlertaNativa(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        notificationId: Int,
        title: String,
        body: String
    ) {
        val appContext = context.applicationContext

        val intent = Intent(appContext, BookingAlarmReceiver::class.java).apply {
            putExtra("NOTIFICATION_TITLE", title)
            putExtra("NOTIFICATION_BODY", body)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    // Función auxiliar para mantener limpio el flujo de cancelación
    private fun cancelarAlarmaExistente(context: Context, alarmManager: AlarmManager, idAlerta: Int) {
        val appContext = context.applicationContext
        val intentCancel = Intent(appContext, BookingAlarmReceiver::class.java)
        val piCancel = PendingIntent.getBroadcast(
            appContext, idAlerta, intentCancel, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (piCancel != null) {
            alarmManager.cancel(piCancel)
            piCancel.cancel()
        }
    }
}