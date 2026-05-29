package com.lksnext.ParkingMMartinez.data

import android.content.Context
import com.lksnext.ParkingMMartinez.model.Reservation
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.time.LocalTime

class BookingManager(context: Context) {
    private val prefs = context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer<LocalTime> { json, _, _ ->
            LocalTime.parse(json.asString)
        })
        .create()

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
        val allBookings = getAllBookings() // Obtenemos lista inmutable primero

        allBookings.find { it.id == reservationId }?.let {
            ParkingManager.releaseSpot(it.spotNumber)
        }

        val updatedBookings = allBookings.filter { it.id != reservationId }
        val json = gson.toJson(updatedBookings)
        prefs.edit().putString("bookings_list", json).apply()
    }

    fun getUserBookings(userId: String): List<Reservation> {
        val all = getAllBookings()
        return all.filter { it.vehicle.id == userId }
        // OJO: Asegúrate de que al crear la reserva, el vehicle.id coincida con el userId
    }
}