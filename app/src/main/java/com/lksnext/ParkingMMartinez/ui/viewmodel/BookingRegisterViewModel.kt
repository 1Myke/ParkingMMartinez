package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.Reservation

class BookingRegisterViewModel : ViewModel() {

    var reservations by mutableStateOf(listOf<Reservation>())
        private set

    // Carga las reservas desde la memoria local
    fun loadReservations(context: Context) {
        val manager = BookingManager(context)
        reservations = manager.getAllBookings()
    }

    // Borra la reserva y actualiza la lista inmediatamente
    fun cancelReservation(context: Context, reservationId: String) {
        val manager = BookingManager(context)
        manager.cancelReservation(reservationId)
        // Refrescar la lista local para que el cambio se vea al instante
        loadReservations(context)
    }

    fun doCheckIn(reservationId: String) {
        // MEJORA: Aquí irá la lógica de Firebase más adelante
        android.util.Log.d("CHECKIN", "Haciendo check-in de: $reservationId")
    }
}