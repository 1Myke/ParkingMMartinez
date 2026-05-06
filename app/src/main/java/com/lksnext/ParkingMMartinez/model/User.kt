package com.lksnext.ParkingMMartinez.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val pass: String = "", //MEJORA OBVIA: HAY QUE HASHEAR LAS CONTRASEÑAS
    //val vehicles: List<Vehicle> = emptyList() con el id confirmamos de quien es cada coche en el vehicle manager
    val avatarURL: String? = null
)