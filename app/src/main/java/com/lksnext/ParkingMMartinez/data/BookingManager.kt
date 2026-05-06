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
        val json = prefs.getString("booking_list", null) ?: return emptyList()
        val type = object : TypeToken<List<Reservation>>() {}.type
        return gson.fromJson(json, type)
    }
}