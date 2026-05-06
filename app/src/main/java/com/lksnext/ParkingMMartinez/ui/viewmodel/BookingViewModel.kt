package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.ZoneNames
import java.util.*
import java.text.SimpleDateFormat
import java.time.LocalTime

class BookingViewModel: ViewModel() {
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
        duration = if (newDuration > 9f) 9f else newDuration
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
        context: Context,
        vehicle: Vehicle,
        zone: ParkingZone,
        onComplete: () -> Unit
    ) {
        val bookingManager = BookingManager(context)

        val zoneType = when (parkingZone) {
            ZoneNames.DISABILITY -> com.lksnext.ParkingMMartinez.model.VehicleType.ADAPTED
            ZoneNames.EV -> com.lksnext.ParkingMMartinez.model.VehicleType.ELECTRIC
            ZoneNames.MOTORCYCLE -> com.lksnext.ParkingMMartinez.model.VehicleType.MOTORCYCLE
            ZoneNames .STANDARD-> com.lksnext.ParkingMMartinez.model.VehicleType.STANDARD
            else -> {
                // Por si llega algo rarete
                android.util.Log.e("MAP_ERROR", "Nombre de zona no reconocido: $parkingZone")
                com.lksnext.ParkingMMartinez.model.VehicleType.STANDARD
            }
        }

        val assignedSpot = com.lksnext.ParkingMMartinez.data.ParkingMock.occupyFirstAvailableSpot(zoneType) ?: 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

        val newReservation = com.lksnext.ParkingMMartinez.model.Reservation(
            vehicle = vehicle,
            zone = zone,
            date = calendar.time,
            startTime = LocalTime.of(startHour, startMinute),
            endTime = LocalTime.parse(getEndTime()),
            isCheckedIn = false,
            spotNumber = assignedSpot
        )

        bookingManager.saveReservation(newReservation)

        hasActiveReservation = true
//        val jsonTest = com.google.gson.Gson().toJson(newReservation)
//        android.util.Log.d("RESERVA", "Guardando reserva: $jsonTest")
        onComplete()
    }

    fun checkUserReservationStatus(context: Context) {
        val bookings = BookingManager(context).getAllBookings()
        // Si hay alguna reserva cuyo tiempo de fin es posterior a "ahora", bloqueamos
        // De momento, si la lista no está vacía, bloqueamos pero esto solo es para probar
        hasActiveReservation = bookings.isNotEmpty()
    }

}