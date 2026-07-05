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
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
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
    private val sessionManager: SessionManager,
    private val notificationRepository: NotificationRepository
): ViewModel() {

    private val halfCapacityNotifiedZones = mutableSetOf<String>()

    private fun dateKey(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    private fun displayDate(date: Date): String =
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(date)

    private fun guardKey(zoneName: String, date: Date) = "$zoneName|${dateKey(date)}"
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

    var isLoading by mutableStateOf(false)
        private set

    private var cachedBookings: List<Reservation> = emptyList()
    private var isInitialDataLoaded = false

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

    fun onVehicleSelected(vehicle: Vehicle) {
        selectedVehicle = vehicle
        if (isInitialDataLoaded) validateBooking()
    }

    fun onTimeChange(h: Int, m: Int) {
        startHour = h
        startMinute = m
        if (isInitialDataLoaded) validateBooking()
    }

    fun onDurationChange(newDuration: Float) {
        duration = newDuration.coerceIn(1.0f, 8.0f)
        if (isInitialDataLoaded) validateBooking()
    }

    fun onDateSelected(date: Date) {
        selectedDate = date
        if (isInitialDataLoaded) validateBooking()
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

    private suspend fun executeSaveWithRetries(
        userId: String, vehicleWithId: Vehicle, zone: ParkingZone,
        start: LocalTime, end: LocalTime, calendarTime: Date
    ): Pair<Reservation?, List<Reservation>> {
        var isSaved = false
        var retries = 0
        var finalReservation: Reservation? = null
        var finalBookings: List<Reservation> = emptyList()

        while (!isSaved && retries < 3) {
            val (allBookings, version) = repository.getAllReservationsWithVersion()

            if (checkUserHasActiveReservationDb(allBookings, userId)) {
                return Pair(null, emptyList())
            }

            val calculatedSpotNumber = ParkingManager.findFirstAvailableSpotNumber(
                allBookings = allBookings, zoneName = parkingZone, vehicleType = vehicleWithId.type,
                selectedDate = calendarTime, slotStart = start, slotEnd = end, editingReservationId = editingReservationId
            )

            val newReservation = Reservation(
                id = editingReservationId ?: UUID.randomUUID().toString(),
                vehicle = vehicleWithId, zone = zone, date = calendarTime,
                startTime = start, endTime = end, isCheckedIn = isEditingCheckedIn, spotNumber = calculatedSpotNumber
            )

            isSaved = repository.trySaveReservationAtomic(newReservation, version)
            if (isSaved) {
                finalReservation = newReservation
                finalBookings = allBookings
            } else {
                retries++
                kotlinx.coroutines.delay((100..300).random().toLong())
            }
        }

        if (!isSaved || finalReservation == null) {
            val allBookings = repository.getAllReservations()
            val fallbackSpot = ParkingManager.findFirstAvailableSpotNumber(
                allBookings, parkingZone, vehicleWithId.type, calendarTime, start, end, editingReservationId
            )
            finalReservation = Reservation(
                id = editingReservationId ?: UUID.randomUUID().toString(),
                vehicle = vehicleWithId, zone = zone, date = calendarTime, startTime = start, endTime = end,
                isCheckedIn = isEditingCheckedIn, spotNumber = fallbackSpot
            )
            repository.saveReservation(finalReservation)
            finalBookings = allBookings
        }

        return Pair(finalReservation, finalBookings)
    }

    private fun checkUserHasActiveReservationDb(allBookings: List<Reservation>, userId: String): Boolean {
        val nowMillis = System.currentTimeMillis()
        return allBookings.any { booking ->
            val isUserReservation = booking.vehicle.userId == userId
            val isTotallyFinished = isReservationPastEntirely(booking, nowMillis)
            val isNotSelf = booking.id != editingReservationId
            isUserReservation && !isTotallyFinished && isNotSelf
        }
    }

    fun confirmReservation(
        context: Context,
        vehicle: Vehicle,
        zone: ParkingZone,
        onComplete: () -> Unit
    ) {
        if (isLoading) return
        isLoading = true

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
            try {
                val (finalReservation, finalBookings) = executeSaveWithRetries(
                    userId, vehicleWithId, zone, start, end, calendar.time
                )

                if (finalReservation == null) {
                    hasActiveReservation = true
                    isLoading = false
                    return@launch
                }

                checkAndBroadcastIfHalfCapacity(context, finalBookings, finalReservation)
                programarAlertasDeReserva(context, finalReservation)

                editingReservationId = null
                isEditingCheckedIn = false
                hasActiveReservation = true

                isLoading = false
                onComplete()
            } catch (e: Exception) {
                android.util.Log.e("BOOKING_ERROR", "Error al guardar reserva: ${e.message}")
                isLoading = false
            }
        }
    }
    
    fun resetLoadingState() {
        isLoading = false
    }

    private fun checkAndBroadcastIfHalfCapacity(
        context: Context,
        existingBookings: List<Reservation>,
        newReservation: Reservation
    ) {
        val vehicleType = newReservation.vehicle.type
        val zoneName    = newReservation.zone.name
        val key         = guardKey(zoneName, newReservation.date)

        if (key in halfCapacityNotifiedZones) return

        val allIncludingNew = existingBookings + newReservation
        ParkingManager.syncWithReservationsForTimeSlot(
            allBookings  = allIncludingNew,
            selectedDate = newReservation.date,
            slotStart    = newReservation.startTime,
            slotEnd      = newReservation.endTime
        )

        val totalSpots     = ParkingManager.getTotalSpotsCount(vehicleType)
        val availableSpots = ParkingManager.getAvailableSpotsCount(vehicleType)
        val occupiedCount  = totalSpots - availableSpots

        android.util.Log.d(
            "BOOKING_HALF_CAPACITY",
            "Zona '$zoneName' (${displayDate(newReservation.date)}): $occupiedCount/$totalSpots ocupadas."
        )

        if (totalSpots > 0 && occupiedCount * 2 >= totalSpots) {
            halfCapacityNotifiedZones.add(key)
            val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(newReservation.date)
            notificationRepository.sendBroadcastNotification(
                context,
                R.string.notification_half_capacity_title,
                R.string.notification_half_capacity_body,
                listOf(zoneName, dateIso)
            )
            android.util.Log.d(
                "BOOKING_HALF_CAPACITY",
                "Broadcast para '$zoneName' el ${displayDate(newReservation.date)} ($occupiedCount/$totalSpots)."
            )
        }
    }


    fun onReservationCancelled(context: Context, cancelledReservation: Reservation) {
        viewModelScope.launch {
            try {
                val remainingBookings = repository.getAllReservations()
                val vehicleType       = cancelledReservation.vehicle.type
                val zoneName          = cancelledReservation.zone.name
                val key               = guardKey(zoneName, cancelledReservation.date)
                val iso               = dateKey(cancelledReservation.date)

                ParkingManager.syncWithReservationsForTimeSlot(
                    allBookings  = remainingBookings,
                    selectedDate = cancelledReservation.date,
                    slotStart    = cancelledReservation.startTime,
                    slotEnd      = cancelledReservation.endTime
                )

                val totalSpots     = ParkingManager.getTotalSpotsCount(vehicleType)
                val availableSpots = ParkingManager.getAvailableSpotsCount(vehicleType)
                val occupiedCount  = totalSpots - availableSpots

                // ── 1. Reset 50 % broadcast guard ───────────────────────────
                if (key in halfCapacityNotifiedZones && totalSpots > 0 && occupiedCount * 2 < totalSpots) {
                    halfCapacityNotifiedZones.remove(key)
                    android.util.Log.d(
                        "BOOKING_HALF_CAPACITY",
                        "Guard reseteado para '$zoneName' el ${displayDate(cancelledReservation.date)} ($occupiedCount/$totalSpots)."
                    )
                }

                // ── 2. Notify bell subscribers if zone now has availability ──
                if (availableSpots > 0) {
                    notificationRepository.notifyAndClearZoneSubscribers(
                        context       = context,
                        zoneName      = zoneName,
                        dateKey       = iso,
                        titleResId    = R.string.notification_zone_available_title,
                        bodyResId     = R.string.notification_zone_available_body,
                        bodyFormatArgs = listOf(zoneName, iso)
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("BOOKING_HALF_CAPACITY", "Error en onReservationCancelled", e)
            }
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
                val isNotSelf = booking.id != editingReservationId

                isUserReservation && !isTotallyFinished && isNotSelf
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
                val currentSelectionStillValid = filtered.any { it.plate == selectedVehicle?.plate }

                if (!currentSelectionStillValid) {
                    selectedVehicle = if (filtered.size == 1) {
                        // 1 solo vehiculo, se selecciona automaticamente
                        filtered[0]
                    } else {
                    // El usuario elige el vehiculo que quiere
                        null
                    }
                } else {
                    selectedVehicle = filtered.find { it.plate == selectedVehicle?.plate }
                }

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
        if (!isInitialDataLoaded) return
        isButtonEnabled = performValidationLocally()
    }

    private fun performValidationLocally(): Boolean {
        if (isLoading) return false

        isOverlapConflict = false
        nextCollisionTime = null
        maxAllowedHours = 0

        if (!isDateTimeValid() || selectedVehicle == null) return false

        if (hasActiveReservation) {
            return false
        }

        val proposedStart = LocalTime.of(startHour, startMinute)
        val proposedEnd = proposedStart.plusHours(duration.toLong())
        val mockZone = ParkingManager.zones.find { it.name == parkingZone }
        val maxSpotsInZone = mockZone?.totalSpots ?: 4

        val nowMillis = System.currentTimeMillis()

        val conflictingBookings = cachedBookings.filter { booking ->
            val calProposed = Calendar.getInstance().apply { time = selectedDate }
            val calBooking = Calendar.getInstance().apply { time = booking.date }
            val isConflictingUserPresent = !isReservationPastEntirely(booking, nowMillis) || booking.isCheckedIn
            val isNotSelf = booking.id != editingReservationId

            calProposed.get(Calendar.YEAR) == calBooking.get(Calendar.YEAR) &&
                    calProposed.get(Calendar.DAY_OF_YEAR) == calBooking.get(Calendar.DAY_OF_YEAR) &&
                    booking.zone.name == parkingZone &&
                    isConflictingUserPresent &&
                    isNotSelf
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

        // --- 1. CONFIGURAR NUEVA ALERTA DE INICIO (30 minutos antes) ---
        val calInicio = Calendar.getInstance().apply {
            time = reservation.date
            set(Calendar.HOUR_OF_DAY, reservation.startTime.hour)
            set(Calendar.MINUTE, reservation.startTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -30) //- 30
        }

        if (calInicio.timeInMillis > nowMillis) {
            val horaFormateada = String.format(Locale.getDefault(), TIME_FORMAT, reservation.startTime.hour, reservation.startTime.minute)

            // 🔒 Pasamos solo IDs lógicos y argumentos, nada de Strings fijos
            configurarAlertaNativa(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = calInicio.timeInMillis,
                notificationId = idAlertaInicio,
                titleResId = R.string.notification_title_start,
                bodyResId = R.string.notification_body_start,
                // Pasamos los argumentos que necesita el string (la zona y la hora)
                args = arrayOf(reservation.zone.name, horaFormateada)
            )
        }

        // --- 2. CONFIGURAR NUEVA ALERTA DE RECORDATORIO CHECK-IN (+15 minutos de la hora de inicio) ---
        if (!reservation.isCheckedIn) {
            val calCheckIn = Calendar.getInstance().apply {
                time = reservation.date
                set(Calendar.HOUR_OF_DAY, reservation.startTime.hour)
                set(Calendar.MINUTE, reservation.startTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, 15) // 15
            }

            if (calCheckIn.timeInMillis > nowMillis) {
                configurarAlertaNativa(
                    context = context,
                    alarmManager = alarmManager,
                    triggerAtMillis = calCheckIn.timeInMillis,
                    notificationId = idAlertaCheckIn,
                    titleResId = R.string.notification_title_checkin_reminder,
                    bodyResId = R.string.notification_body_checkin_reminder,
                    args = arrayOf(reservation.zone.name),
                    reservationId = reservation.id
                )
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
            add(Calendar.MINUTE, -15) // - 15
        }

        if (calFin.timeInMillis > nowMillis) {
            configurarAlertaNativa(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = calFin.timeInMillis,
                notificationId = idAlertaFin,
                titleResId = R.string.notification_title_end,
                bodyResId = R.string.notification_body_end
            )
        }
    }

    // Funcion para configurar la alerta nativa
    private fun configurarAlertaNativa(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        notificationId: Int,
        titleResId: Int,
        bodyResId: Int,
        args: Array<String> = emptyArray(),
        reservationId: String? = null
    ) {
        val appContext = context.applicationContext

        val intent = Intent(appContext, BookingAlarmReceiver::class.java).apply {
            // Mandamos los recursos lógicos, no textos fijos pre-renderizados
            putExtra("NOTIFICATION_TITLE_RES", titleResId)
            putExtra("NOTIFICATION_BODY_RES", bodyResId)
            putExtra("NOTIFICATION_ARGS", args)
            if (reservationId != null) {
                putExtra("RESERVATION_ID", reservationId)
            }
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
            appContext, idAlerta, intentCancel, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(piCancel)
        piCancel.cancel()
    }

    fun inicializarPantalla(initialZone: String, initialHour: Int, initialMinute: Int) {
        setZone(initialZone)

        if (editingReservationId == null) {
            cancelEditing()
            startHour = initialHour
            startMinute = initialMinute
        }

        // 🛡️ Abrimos UN SOLO hilo para garantizar el orden en el celular
        viewModelScope.launch {
            try {
                isLoading = true
                val currentUserId = sessionManager.getActiveUserId() ?: ""
                cachedBookings = repository.getAllReservations()
                val nowMillis = System.currentTimeMillis()

                hasActiveReservation = cachedBookings.any { booking ->
                    val isUserReservation = booking.vehicle.userId == currentUserId
                    val isTotallyFinished = isReservationPastEntirely(booking, nowMillis)
                    val isNotSelf = booking.id != editingReservationId
                    isUserReservation && !isTotallyFinished && isNotSelf
                }

                val allVehicles = vehicleRepository.getVehicles(currentUserId)
                val requiredType = when (parkingZone) {
                    "Disability", ZoneNames.DISABILITY -> VehicleType.ADAPTED
                    "EV", ZoneNames.EV -> VehicleType.ELECTRIC
                    "Motorcycle", ZoneNames.MOTORCYCLE -> VehicleType.MOTORCYCLE
                    "Standard", ZoneNames.STANDARD -> VehicleType.STANDARD
                    else -> VehicleType.STANDARD
                }

                val filtered = allVehicles.filter { it.type == requiredType }
                userVehicles = filtered

                val currentSelectionStillValid = filtered.any { it.plate == selectedVehicle?.plate }
                if (!currentSelectionStillValid) {
                    selectedVehicle = if (filtered.size == 1) filtered[0] else null
                } else {
                    selectedVehicle = filtered.find { it.plate == selectedVehicle?.plate }
                }

                isInitialDataLoaded = true

            } catch (e: Exception) {
                android.util.Log.e("FIREBASE_ERROR", "Error: ${e.message}")
            } finally {
                isLoading = false
                validateBooking()
            }
        }
    }
}