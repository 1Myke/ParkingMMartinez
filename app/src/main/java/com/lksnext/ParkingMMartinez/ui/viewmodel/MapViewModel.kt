package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MapViewModel(
    private val repository: BookingRepository
) : ViewModel() {

    var selectedDate by mutableStateOf(Date())
        private set

    var selectedStartTime by mutableStateOf(LocalTime.now())
        private set

    val selectedEndTime: LocalTime
        get() = selectedStartTime.plusHours(1)

    var zones by mutableStateOf(ParkingMock.zones)
        private set

    var showTimePicker by mutableStateOf(false)

    var availableDates by mutableStateOf<List<Pair<Int, String>>>(emptyList())
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
        val list = mutableListOf<Pair<Int, String>>()
        val calendar = Calendar.getInstance()

        selectedDayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        list.add(Pair(selectedDayNumber, "TODAY"))

        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
            val label = dateFormat.format(calendar.time)
            list.add(Pair(dayNum, label))
        }
        availableDates = list
    }

    fun onDateSelected(dayNumber: Int) {
        selectedDayNumber = dayNumber

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (dayNumber >= currentDay) {
            calendar.set(Calendar.DAY_OF_MONTH, dayNumber)
        } else {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, dayNumber)
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        selectedDate = calendar.time
        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    fun onTimeChange(hour: Int, minute: Int) {
        selectedStartTime = LocalTime.of(hour, minute)
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
        val allBookings = repository.getAllReservations()

        ParkingMock.syncWithReservationsForTimeSlot(
            allBookings = allBookings,
            selectedDate = selectedDate,
            slotStart = selectedStartTime,
            slotEnd = selectedEndTime
        )

        zones = ParkingMock.zones.toList()
    }
}