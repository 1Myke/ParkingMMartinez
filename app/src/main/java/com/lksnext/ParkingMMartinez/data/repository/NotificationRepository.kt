package com.lksnext.ParkingMMartinez.data.repository

import com.lksnext.ParkingMMartinez.model.NotificationItem

interface NotificationRepository {
    fun saveNotification(notification: NotificationItem)
    fun getNotificationsFlow(userId: String, onUpdate: (List<NotificationItem>) -> Unit)
    fun markAllAsRead(userId: String)
    fun deleteNotification(notificationId: String)
    fun deleteAllNotifications(userId: String)
    fun linkDeviceWithUser(userId: String)
    fun sendPushNotification(context: android.content.Context, targetUserId: String, title: String, message: String)

    fun sendBroadcastNotification(context: android.content.Context, title: String, message: String)
}