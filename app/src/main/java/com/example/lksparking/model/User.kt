package com.example.lksparking.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val vehicles: List<Vehicle> = emptyList()
)