package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.data.UserManager
import com.lksnext.ParkingMMartinez.data.UserMock
import com.lksnext.ParkingMMartinez.model.User

class LocalUserRepository(context: Context) : UserRepository {
    private val userManager = UserManager(context)

    override fun getAllUsers(): List<User> = userManager.getAllUsers()

    override fun registerUser(user: User) {
        userManager.registerUser(user)
    }

    override fun authenticate(email: String, pass: String): User? {
        return userManager.authenticate(email, pass)
    }

    override fun getUserById(userId: String): User? {
        // Buscamos primero en el manager (SharedPreferences)
        // y si no está, en el Mock (por si son usuarios de prueba)
        return userManager.getAllUsers().find { it.id == userId }
            ?: UserMock.users.find { it.id == userId }
    }
}