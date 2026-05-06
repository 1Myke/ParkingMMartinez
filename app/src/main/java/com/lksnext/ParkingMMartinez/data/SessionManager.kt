package com.lksnext.ParkingMMartinez.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_sessions", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveSession(isLoggedIn: Boolean, userId: String? = null) {
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn)
        if (userId != null) {
            editor.putString(KEY_USER_ID, userId)
        }
        editor.commit()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun getActiveUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}