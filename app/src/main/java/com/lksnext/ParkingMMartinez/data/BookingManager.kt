package com.lksnext.ParkingMMartinez.data

import android.content.Context
import androidx.compose.ui.graphics.PathSegment
import com.lksnext.ParkingMMartinez.model.Reservation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookingManager(context: Context) {
    private val prefs = context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveReservation(reservation: Reservation) {
        val currentBookings = getAllBookings().toMutableList()
        currentBookings.add(reservation)
        val json = gson.toJson(currentBookings)
        prefs.edit().putString("bookings_list", json).apply()
    }

    fun getAllBookings(): List<Reservation> {
        val json = prefs.getString("bookings_list", null)
        if (json.isNullOrEmpty()) return emptyList()

        return try {
            val type = object : TypeToken<List<Reservation>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            android.util.Log.e("GSON_ERROR", "Error parseando reservas: ${e.message}")
            emptyList()
        }
    }

    fun cancelReservation(reservationId: String) {
        val allBookings = getAllBookings().toMutableList()

        val reservationToDelete = allBookings.find { it.id == reservationId }
        reservationToDelete?.let {
            ParkingMock.releaseSpot(it.spotNumber)
        }

        val updatedBookings = allBookings.filter { it.id != reservationId }
        val json = gson.toJson(updatedBookings)
        prefs.edit().putString("bookings_list", json).apply()
    }
}