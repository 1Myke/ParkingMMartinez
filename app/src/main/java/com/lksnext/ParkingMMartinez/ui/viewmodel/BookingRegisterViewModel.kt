package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.data.ParkingMock
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation

class BookingRegisterViewModel(
    private val repository: BookingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var reservations by mutableStateOf(listOf<Reservation>())
        private set

    // Carga las reservas desde la memoria local
    fun loadReservations() {
        val currentUserId = sessionManager.getActiveUserId()

        if (currentUserId == null) {
            reservations = emptyList()
            return
        }

        reservations = repository.getUserReservations(currentUserId)
    }

    // Borra la reserva y actualiza la lista inmediatamente
    fun cancelReservation(reservationId: String) {
        repository.cancelReservation(reservationId)
        // Refrescar la lista local para que el cambio se vea al instante
        loadReservations()
    }

    fun doCheckIn(reservationId: String) {
        // MEJORA: Aquí irá la lógica de Firebase más adelante
        android.util.Log.d("CHECKIN", "Haciendo check-in de: $reservationId")
    }
}