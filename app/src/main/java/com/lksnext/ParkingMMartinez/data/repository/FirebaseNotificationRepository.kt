package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lksnext.ParkingMMartinez.model.NotificationItem
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import org.json.JSONObject
import org.json.JSONArray
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FirebaseNotificationRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("notifications")

    override fun saveNotification(notification: NotificationItem) {
        val docRef = collection.document()
        val finalNotification = notification.copy(id = docRef.id)
        docRef.set(finalNotification)
    }

    override fun getNotificationsFlow(userId: String, onUpdate: (List<NotificationItem>) -> Unit) {
        collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot.toObjects(NotificationItem::class.java)

                val sortedItems = items.sortedByDescending { it.timestamp?.time ?: 0L }
                onUpdate(sortedItems)
            }
    }

    override fun markAllAsRead(userId: String) {
        collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }

    override fun deleteNotification(notificationId: String) {
        collection.document(notificationId).delete()
    }

    override fun deleteAllNotifications(userId: String) {
        collection.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }

    override fun linkDeviceWithUser(userId: String) {
        val onesignalSubscriptionId = OneSignal.User.pushSubscription.id

        if (!onesignalSubscriptionId.isNullOrEmpty()) {
            val data = mapOf("onesignal_id" to onesignalSubscriptionId)
            firestore.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("OneSignal_Firestore", "¡ID guardado con éxito!: $onesignalSubscriptionId")
                }
                .addOnFailureListener { e ->
                    Log.e("OneSignal_Firestore", "Error al guardar en Firestore", e)
                }
        }
    }

    override fun sendPushNotification(
        context: Context,
        targetUserId: String,
        title: String,
        message: String
    ) {
        firestore.collection("users").document(targetUserId).get()
            .addOnSuccessListener { document ->
                val targetOneSignalId = document.getString("onesignal_id")

                if (!targetOneSignalId.isNullOrEmpty()) {
                    CoroutineScope(ioDispatcher).launch {
                        try {
                            val resId = context.resources.getIdentifier(
                                "onesignal_app_id_secret",
                                "string",
                                context.packageName
                            )
                            val appId = if (resId != 0) context.getString(resId) else ""
                            if (appId.isEmpty()) return@launch

                            val url = URL("https://onesignal.com")
                            val conn = url.openConnection() as HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.setRequestProperty(
                                "Content-Type",
                                "application/json; charset=UTF-8"
                            )
                            conn.doOutput = true

                            val jsonHeadings = JSONObject().put("en", title)
                            val jsonContents = JSONObject().put("en", message)

                            val jsonBody = JSONObject().apply {
                                put("app_id", appId)
                                put("include_subscription_ids", JSONArray().put(targetOneSignalId))
                                put("headings", jsonHeadings)
                                put("contents", jsonContents)
                                put("android_sound", "notification")
                                put("priority", 10)
                            }

                            OutputStreamWriter(conn.outputStream).use { writer ->
                                writer.write(jsonBody.toString())
                                writer.flush()
                            }

                            val code = conn.responseCode
                            Log.d("OneSignal_Push", "Resultado envío API: Código $code")
                            conn.disconnect()

                        } catch (e: Exception) {
                            Log.e("OneSignal_Push", "Error crítico al enviar HTTP POST", e)
                        }
                    }
                }
            }
    }
}