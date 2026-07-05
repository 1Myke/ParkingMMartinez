package com.lksnext.ParkingMMartinez.repository

fun interface ChatRepository {
    suspend fun sendFAQ(message: String): Result<String>
}