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

class BookingRegisterViewModel(
    private val repository: BookingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var reservations by mutableStateOf(listOf<Reservation>())
        private set

    fun loadReservations() {
        val currentUserId = sessionManager.getActiveUserId()

        if (currentUserId == null) {
            reservations = emptyList()
            return
        }

        viewModelScope.launch {
            reservations = repository.getUserReservations(currentUserId)
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            repository.cancelReservation(reservationId)
            loadReservations()
        }
    }

    fun doCheckIn(reservationId: String) {
        android.util.Log.d("CHECKIN", "Haciendo check-in de: $reservationId")
    }
}