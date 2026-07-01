package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.User

interface UserRepository {
    // Ya no necesitamos getAllUsers() porque en Firebase no listamos todos los usuarios por seguridad
    suspend fun registerUser(user: User): Boolean
    suspend fun authenticate(email: String, pass: String): User?
    suspend fun getUserById(userId: String): User?
    suspend fun updateAvatar(userId: String, url: String): Boolean
}