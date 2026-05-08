package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.Reservation

interface BookingRepository {
    fun getAllReservations(): List<Reservation>
    fun saveReservation(reservation: Reservation)
    fun cancelReservation(reservationId: String)
    fun getUserReservations(userId: String): List<Reservation>
}

// La semana que viene aquí añadire funciones suspend para Firebase

