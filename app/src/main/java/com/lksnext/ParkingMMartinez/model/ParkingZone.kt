package com.lksnext.ParkingMMartinez.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.Exclude

@Keep
class ParkingZone(
    val name: String = "",
    val availableSpots: Int = 0,
    val totalSpots: Int = 0,
    @get:Exclude @set:Exclude var iconRes: Int = 0, // Para el icono de los vehiculos
    @get:Exclude @set:Exclude var color: Color
) {
    constructor(): this("", 0, 0, 0, Color.Magenta)
    fun copy(): ParkingZone {
        val newZone = ParkingZone(
            name = this.name,
            availableSpots = this.availableSpots,
            totalSpots = this.totalSpots,
            iconRes = this.iconRes,
            color = this.color
        )
        return newZone
    }
}
