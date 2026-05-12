package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.User

interface UserRepository {
    fun getAllUsers(): List<User>
    fun registerUser(user: User)
    fun authenticate(email: String, pass: String): User?
    fun getUserById(userId: String): User?
}