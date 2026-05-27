package com.lksnext.ParkingMMartinez.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val pass: String = "", //MEJORA OBVIA: HAY QUE HASHEAR LAS CONTRASEÑAS
    val avatarURL: String? = null
)