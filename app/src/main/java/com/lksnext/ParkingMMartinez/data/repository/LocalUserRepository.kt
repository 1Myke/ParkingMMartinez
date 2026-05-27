package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.UserManager
import com.lksnext.ParkingMMartinez.data.UserMock
import com.lksnext.ParkingMMartinez.model.User

class LocalUserRepository(context: Context) : UserRepository {
    private val userManager = UserManager(context)

    override suspend fun registerUser(user: User): Boolean {
        return try {
            userManager.registerUser(user)
            true
        } catch (e : Exception) {
            false
        }
    }

    override suspend fun authenticate(email: String, pass: String): User? {
        return userManager.authenticate(email, pass)
    }

    override suspend fun getUserById(userId: String): User? {
        return userManager.getAllUsers().find { it.id == userId }
            ?: UserMock.users.find { it.id == userId }
    }
}