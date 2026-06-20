package com.lksnext.ParkingMMartinez.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val isRead: Boolean = false
)