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

    // ── Zone-availability bell feature ──────────────────────────────────────

    /** Subscribes [userId] to receive a one-time push when [zoneName] has availability on [dateKey]. */
    fun subscribeToZoneAvailability(
        userId: String,
        onesignalId: String,
        languageCode: String,
        zoneName: String,
        dateKey: String
    )

    /** Removes the subscription for [userId] + [zoneName] + [dateKey]. */
    fun unsubscribeFromZoneAvailability(userId: String, zoneName: String, dateKey: String)

    /** Returns the set of zone names the user has subscribed to for [dateKey]. */
    fun getUserZoneSubscriptions(userId: String, dateKey: String, onResult: (Set<String>) -> Unit)

    /**
     * Sends a push notification to every subscriber for [zoneName] + [dateKey],
     * saves a [NotificationItem] per user, then deletes the subscription documents
     * (one-time fire-and-forget alert).
     */
    fun notifyAndClearZoneSubscribers(
        context: android.content.Context,
        zoneName: String,
        dateKey: String,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    )
}