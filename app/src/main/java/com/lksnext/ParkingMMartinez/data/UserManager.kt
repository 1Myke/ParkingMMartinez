package com.lksnext.ParkingMMartinez.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lksnext.ParkingMMartinez.model.User

class UserManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAllUsers(): List<User> {
        val json = prefs.getString("registered_users", null) ?: return emptyList()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type)
    }

    fun registerUser(user: User) {
        val currentUsers = getAllUsers().toMutableList()
        // Evitamos duplicados por email
        if (currentUsers.none { it.email == user.email }) {
            currentUsers.add(user)
            val json = gson.toJson(currentUsers)
            prefs.edit().putString("registered_users", json).apply()
        }
    }

    fun authenticate(email: String, pass: String): User? {
        return getAllUsers().find { it.email == email && it.pass == pass }
    }
}