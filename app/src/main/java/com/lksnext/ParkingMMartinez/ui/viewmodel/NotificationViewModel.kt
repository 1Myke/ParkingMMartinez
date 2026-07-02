package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.model.NotificationItem

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set

    var isBatteryOptimized by mutableStateOf(false)
        private set

    private var isInitialized = false

    fun initViewModel(context: Context) {
        checkBatteryStatus(context)
        if (!isInitialized) {
            loadNotifications()
            isInitialized = true
        }
    }

    private fun loadNotifications() {
        val userId = sessionManager.getActiveUserId() ?: return
        repository.getNotificationsFlow(userId) { list ->
            Handler(Looper.getMainLooper()).post {
                notifications = list
            }
        }
    }

    fun checkBatteryStatus(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isBatteryOptimized = !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            isBatteryOptimized = false
        }
    }

    fun markAllAsRead() {
        val userId = sessionManager.getActiveUserId() ?: return
        repository.markAllAsRead(userId)
    }

    fun deleteNotification(notificationId: String) {
        repository.deleteNotification(notificationId)
    }

    fun deleteAllNotifications() {
        val userId = sessionManager.getActiveUserId() ?: return
        repository.deleteAllNotifications(userId)
    }
}