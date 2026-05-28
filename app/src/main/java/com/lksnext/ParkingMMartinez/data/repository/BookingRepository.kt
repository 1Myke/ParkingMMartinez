package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.Reservation

interface BookingRepository {
    suspend fun getAllReservations(): List<Reservation>
    suspend fun saveReservation(reservation: Reservation)
    suspend fun cancelReservation(reservationId: String)
    suspend fun getUserReservations(userId: String): List<Reservation>
}

// La semana que viene aquí añadire funciones suspend para Firebase

