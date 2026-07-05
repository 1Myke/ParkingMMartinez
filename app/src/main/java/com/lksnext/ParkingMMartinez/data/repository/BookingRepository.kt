package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.Reservation

interface BookingRepository {
    suspend fun getAllReservations(): List<Reservation>
    suspend fun getAllReservationsWithVersion(): Pair<List<Reservation>, Long> {
        return Pair(getAllReservations(), 0L)
    }
    suspend fun saveReservation(reservation: Reservation)
    suspend fun trySaveReservationAtomic(reservation: Reservation, expectedVersion: Long): Boolean {
        saveReservation(reservation)
        return true
    }
    suspend fun cancelReservation(reservationId: String)
    suspend fun getUserReservations(userId: String): List<Reservation>
}

// La semana que viene aquí añadire funciones suspend para Firebase
