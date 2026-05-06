package com.lksnext.ParkingMMartinez.data

import com.lksnext.ParkingMMartinez.model.User

object UserMock {
    // Falsa lista de usuarios para pruebas
    val users = listOf(
        User(
            id = "mikel_id_123",
            name = "Mikel",
            lastName = "Martinez",
            email = "mikel@lksnext.com",
            pass = "22222"
        ),
        User(id = "admin_id_456",
            name = "Admin",
            lastName = "System",
            email = "admin@parking.com",
            pass = "22222"
        )
    )


}