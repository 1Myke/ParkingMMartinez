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

    /**
     * Sends a push to ALL users. The payload contains translations for every supported
     * locale; each device receives the text in its OneSignal language setting.
     * A [NotificationItem] is also saved per user in their stored language preference.
     *
     * ISO-8601 dates in [bodyFormatArgs] ("yyyy-MM-dd") are re-formatted per locale.
     */
    fun sendBroadcastNotification(
        context: android.content.Context,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    )
}