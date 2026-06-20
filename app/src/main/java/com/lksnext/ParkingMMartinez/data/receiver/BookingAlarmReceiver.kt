package com.lksnext.ParkingMMartinez.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lksnext.ParkingMMartinez.MainActivity
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.FirebaseNotificationRepository
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.model.NotificationItem
import com.lksnext.ParkingMMartinez.model.ZoneNames
import java.util.Date

class BookingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionManager = SessionManager(context.applicationContext)
        val userId = sessionManager.getActiveUserId() ?: return // Si no hay sesión activa, ignoramos

        val titleRes = intent.getIntExtra("NOTIFICATION_TITLE_RES", 0)
        val bodyRes = intent.getIntExtra("NOTIFICATION_BODY_RES", 0)
        val args = intent.getStringArrayExtra("NOTIFICATION_ARGS") ?: emptyArray()

        if (titleRes == 0 || bodyRes == 0) return

        val processedArgs = args.map { arg ->
            when (arg) {
                ZoneNames.DISABILITY -> context.getString(R.string.zone_disability)
                ZoneNames.EV -> context.getString(R.string.zone_ev)
                ZoneNames.MOTORCYCLE -> context.getString(R.string.zone_motorcycle)
                ZoneNames.STANDARD -> context.getString(R.string.zone_standard)
                else -> arg
            }
        }.toTypedArray()

        val title = context.getString(titleRes)
        val body = context.getString(bodyRes, *processedArgs)

        val notificationRepo: NotificationRepository = FirebaseNotificationRepository()
        val newNotification = NotificationItem(
            userId = userId,
            title = title,
            body = body,
            timestamp = Date(),
            isRead = false
        )
        notificationRepo.saveNotification(newNotification)

        val channelId = context.getString(R.string.notification_channel_id)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_channel_name)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}