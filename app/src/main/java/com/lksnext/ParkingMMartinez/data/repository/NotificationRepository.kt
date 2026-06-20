package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.NotificationItem

interface NotificationRepository {
    fun saveNotification(notification: NotificationItem)
    fun getNotificationsFlow(userId: String, onUpdate: (List<NotificationItem>) -> Unit)
    fun markAllAsRead(userId: String)
    fun deleteNotification(notificationId: String) // 🌟 NUEVO
    fun deleteAllNotifications(userId: String)     // 🌟 NUEVO
}