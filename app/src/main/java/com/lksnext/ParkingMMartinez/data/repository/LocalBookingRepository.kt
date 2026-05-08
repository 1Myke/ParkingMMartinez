package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.Reservation

class LocalBookingRepository (context: Context): BookingRepository {
    private val bookingManager = BookingManager(context)

    override fun getAllReservations(): List<Reservation> = bookingManager.getAllBookings()

    override fun saveReservation(reservation: Reservation) {
        bookingManager.saveReservation(reservation)
    }

    override fun cancelReservation(reservationId: String) {
        bookingManager.cancelReservation(reservationId)
    }

    override fun getUserReservations(userId: String): List<Reservation> {
        return bookingManager.getUserBookings(userId)
    }
}