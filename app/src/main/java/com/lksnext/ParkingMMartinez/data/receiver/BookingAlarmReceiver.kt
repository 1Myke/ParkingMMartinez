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

class BookingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionManager = SessionManager(context.applicationContext)
        val userId = try {
            sessionManager.getActiveUserId()
        } catch (e: Exception) {
            null
        }

        if (userId == null) {
            android.util.Log.d("ALARM_RECEIVER", "UserId es null en frío. Forzamos muestra para testeo.")
        }

        val defaultTitle = context.getString(R.string.notification_title_default)
        val title = intent.getStringExtra("NOTIFICATION_TITLE") ?: defaultTitle
        val body = intent.getStringExtra("NOTIFICATION_BODY") ?: ""

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
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}