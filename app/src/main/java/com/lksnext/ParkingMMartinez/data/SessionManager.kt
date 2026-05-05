package com.lksnext.ParkingMMartinez.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_sessions", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}