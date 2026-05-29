package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MapViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    var selectedDate by mutableStateOf(Date())
        private set

    var selectedStartTime by mutableStateOf(LocalTime.now().withSecond(0).withNano(0))
        private set

    val selectedEndTime: LocalTime
        get() = selectedStartTime.plusHours(1)

    var zones by mutableStateOf<List<ParkingZone>>(emptyList())
        private set

    var showTimePicker by mutableStateOf(false)

    var availableDates by mutableStateOf<List<Pair<Date, String>>>(emptyList())
        private set

    var selectedDayNumber by mutableStateOf(0)
        private set

    init {
        val cleanCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDate = cleanCalendar.time

        generateAvailableDates()
        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    private fun generateAvailableDates() {
        val list = mutableListOf<Pair<Date, String>>()
        val calendar = Calendar.getInstance()

        selectedDayNumber = calendar.get(Calendar.DAY_OF_MONTH)

        val todayCalendar = calendar.clone() as Calendar
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        list.add(Pair(todayCalendar.time, "TODAY"))

        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val futureDayCalendar = calendar.clone() as Calendar
            futureDayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            futureDayCalendar.set(Calendar.MINUTE, 0)
            futureDayCalendar.set(Calendar.SECOND, 0)
            futureDayCalendar.set(Calendar.MILLISECOND, 0)

            val label = dateFormat.format(futureDayCalendar.time)
            list.add(Pair(futureDayCalendar.time, label))
        }
        availableDates = list
    }

    fun onDateSelected(fullDate: Date) {
        val calendar = Calendar.getInstance().apply { time = fullDate }
        selectedDayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        selectedDate = fullDate

        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    fun onTimeChange(hour: Int, minute: Int) {
        selectedStartTime = LocalTime.of(hour, minute).withSecond(0).withNano(0)
        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    fun refreshParking() {
        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    private suspend fun refreshParkingStatus() {
        try {
            val allBookings = repository.getAllReservations()

            ParkingMock.syncWithReservationsForTimeSlot(
                allBookings = allBookings,
                selectedDate = selectedDate,
                slotStart = selectedStartTime,
                slotEnd = selectedEndTime
            )

            val freshlyCalculatedZones = mutableListOf<ParkingZone>()
            ParkingMock.zones.forEach { zone ->
                freshlyCalculatedZones.add(zone.copy())
            }

            zones = freshlyCalculatedZones

        } catch (e: Exception) {
            android.util.Log.e("MAP_REFRESH_ERROR", "Error refrescando el mapa: ${e.message}")
        }
    }
}