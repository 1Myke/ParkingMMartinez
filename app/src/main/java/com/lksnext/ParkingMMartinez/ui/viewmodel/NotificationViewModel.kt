package com.lksnext.ParkingMMartinez.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationViewModel(
    private val repository: BookingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var missedNotifications by mutableStateOf<List<Reservation>>(emptyList())
        private set

    fun loadMissedReservations() {
        val currentUserId = sessionManager.getActiveUserId() ?: return
        val nowMillis = System.currentTimeMillis()

        viewModelScope.launch {
            val allBookings = repository.getAllReservations()

            missedNotifications = allBookings.filter { booking ->
                val isStartHourPast = Calendar.getInstance().apply {
                    time = booking.date
                    set(Calendar.HOUR_OF_DAY, booking.startTime.hour)
                    set(Calendar.MINUTE, booking.startTime.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis < nowMillis

                booking.vehicle.userId == currentUserId && !booking.isCheckedIn && isStartHourPast
            }
        }
    }
}