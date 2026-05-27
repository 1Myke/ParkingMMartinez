package com.lksnext.ParkingMMartinez.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lksnext.ParkingMMartinez.model.Vehicle

class VehicleManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vehicle_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getVehicles(userId: String): List<Vehicle> {
        val json = prefs.getString("vehicles_$userId", null) ?: return emptyList()
        val type = object : TypeToken<List<Vehicle>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addVehicle(userId: String, vehicle: Vehicle) {
        val currentList = getVehicles(userId).toMutableList()
        currentList.add(vehicle)

        val json = gson.toJson(currentList)
        prefs.edit().putString("vehicles_$userId", json).apply()
    }

    fun deleteVehicle(userId: String, vehicle: Vehicle) {
        val currentList = getVehicles(userId).toMutableList()
        currentList.removeAll { it.id == vehicle.id }

        val json = gson.toJson(currentList)
        prefs.edit().putString("vehicles_$userId", json).apply()
    }
}