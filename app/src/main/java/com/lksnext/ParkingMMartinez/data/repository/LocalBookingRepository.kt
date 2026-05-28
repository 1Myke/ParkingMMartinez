package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.Reservation

class LocalBookingRepository (context: Context): BookingRepository {
    private val bookingManager = BookingManager(context)

    override suspend fun getAllReservations(): List<Reservation> = bookingManager.getAllBookings()

    override suspend fun saveReservation(reservation: Reservation) {
        bookingManager.saveReservation(reservation)
    }

    override suspend fun cancelReservation(reservationId: String) {
        bookingManager.cancelReservation(reservationId)
    }

    override suspend fun getUserReservations(userId: String): List<Reservation> {
        return bookingManager.getUserBookings(userId)
    }
}