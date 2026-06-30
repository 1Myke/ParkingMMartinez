package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lksnext.ParkingMMartinez.model.NotificationItem
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
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

    // ─────────────────────────────────────────────────────────────────────────
    // BUG FIX 1: Observer pattern for linkDeviceWithUser
    //
    // The previous implementation read OneSignal.User.pushSubscription.id
    // synchronously at login time. On the first run (or after a cold start)
    // the OneSignal SDK hasn't finished registering with its servers yet, so
    // the ID is null → nothing was ever written to Firestore →
    // sendBroadcastNotification always found an empty list and returned early.
    //
    // Fix: try immediately; if null, register an IPushSubscriptionObserver so
    // the ID is persisted as soon as the SDK assigns one.
    // ─────────────────────────────────────────────────────────────────────────
    override fun linkDeviceWithUser(userId: String) {
        val immediateId = OneSignal.User.pushSubscription.id

        if (!immediateId.isNullOrEmpty()) {
            // ID already available — save right away
            saveOneSignalIdToFirestore(userId, immediateId)
            return
        }

        // SDK hasn't assigned an ID yet — observe and save the moment it does
        Log.d("OneSignal_Firestore", "Subscription ID no disponible aún para user=$userId. Registrando observador...")

        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                val newId = state.current.id
                if (!newId.isNullOrEmpty()) {
                    OneSignal.User.pushSubscription.removeObserver(this)
                    saveOneSignalIdToFirestore(userId, newId)
                }
            }
        }
        OneSignal.User.pushSubscription.addObserver(observer)
    }

    /** Persists [onesignalId] into the user's Firestore document using merge. */
    private fun saveOneSignalIdToFirestore(userId: String, onesignalId: String) {
        Log.d("OneSignal_Firestore", "Guardando onesignal_id='$onesignalId' para user='$userId'…")
        firestore.collection("users").document(userId)
            .set(mapOf("onesignal_id" to onesignalId), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("OneSignal_Firestore", "✅ onesignal_id guardado para user='$userId'")
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal_Firestore", "❌ Error guardando onesignal_id para user='$userId'", e)
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
                            val appId   = resolveResString(context, "onesignal_app_id_secret")
                            val restKey = resolveResString(context, "onesignal_rest_api_key_secret")
                            if (appId.isEmpty()) {
                                Log.e("OneSignal_Push", "App ID vacío — push cancelado.")
                                return@launch
                            }

                            val jsonBody = JSONObject().apply {
                                put("app_id", appId)
                                put("include_subscription_ids", JSONArray().put(targetOneSignalId))
                                put("headings", JSONObject().put("en", title))
                                put("contents", JSONObject().put("en", message))
                                put("android_sound", "notification")
                                put("priority", 10)
                            }

                            val code = postToOneSignal(jsonBody, restKey)
                            Log.d("OneSignal_Push", "Resultado envío: código $code")

                        } catch (e: Exception) {
                            Log.e("OneSignal_Push", "Error crítico al enviar push", e)
                        }
                    }
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUG FIX 2: Authorization header + verbose diagnostic logging
    //
    // The OneSignal REST API /api/v1/notifications requires:
    //   Authorization: Basic <REST_API_KEY>
    // Without it the server returns HTTP 400 and rejects the payload.
    //
    // The REST API Key is read from the string resource
    // "onesignal_rest_api_key_secret" (injected via Gradle resValue — see
    // build.gradle.kts). Add  onesignal.rest.api.key=YOUR_KEY  to
    // gradle.properties (never commit that file).
    // ─────────────────────────────────────────────────────────────────────────
    override fun sendBroadcastNotification(context: Context, title: String, message: String) {
        Log.d("OneSignal_Broadcast", "sendBroadcastNotification() llamado. Consultando Firestore…")

        firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("OneSignal_Broadcast", "Firestore devolvió ${snapshot.size()} documentos de usuarios.")

                val allSubscriptionIds = snapshot.documents
                    .mapNotNull { it.getString("onesignal_id") }
                    .filter { it.isNotBlank() }

                Log.d("OneSignal_Broadcast", "Usuarios con onesignal_id válido: ${allSubscriptionIds.size}")

                if (allSubscriptionIds.isEmpty()) {
                    Log.e(
                        "OneSignal_Broadcast",
                        "❌ Ningún usuario tiene onesignal_id en Firestore. " +
                        "Verifica que linkDeviceWithUser() se ejecutó correctamente y que " +
                        "el campo 'onesignal_id' existe en la colección 'users'."
                    )
                    return@addOnSuccessListener
                }

                CoroutineScope(ioDispatcher).launch {
                    try {
                        val appId   = resolveResString(context, "onesignal_app_id_secret")
                        val restKey = resolveResString(context, "onesignal_rest_api_key_secret")

                        if (appId.isEmpty()) {
                            Log.e("OneSignal_Broadcast", "❌ App ID vacío — broadcast cancelado.")
                            return@launch
                        }
                        if (restKey.isEmpty()) {
                            Log.e(
                                "OneSignal_Broadcast",
                                "❌ REST API Key vacía — el servidor rechazará la petición. " +
                                "Añade  onesignal.rest.api.key=TU_CLAVE  a gradle.properties " +
                                "y asegúrate de que build.gradle.kts genera 'onesignal_rest_api_key_secret'."
                            )
                        }

                        val jsonHeadings = JSONObject().put("en", title)
                        val jsonContents = JSONObject().put("en", message)

                        allSubscriptionIds.chunked(2_000).forEachIndexed { chunkIndex, chunk ->
                            try {
                                val idsArray = JSONArray().apply { chunk.forEach { put(it) } }

                                val jsonBody = JSONObject().apply {
                                    put("app_id", appId)
                                    put("include_subscription_ids", idsArray)
                                    put("headings", jsonHeadings)
                                    put("contents", jsonContents)
                                    put("android_sound", "notification")
                                    put("priority", 10)
                                }

                                Log.d("OneSignal_Broadcast", "Enviando chunk $chunkIndex (${chunk.size} IDs)…")
                                val code = postToOneSignal(jsonBody, restKey)
                                Log.d("OneSignal_Broadcast", "Chunk $chunkIndex → HTTP $code")

                                if (code !in 200..299) {
                                    Log.e("OneSignal_Broadcast", "❌ Chunk $chunkIndex rechazado (HTTP $code). Revisa el REST API Key y el App ID.")
                                } else {
                                    Log.d("OneSignal_Broadcast", "✅ Chunk $chunkIndex enviado con éxito.")
                                }

                            } catch (chunkEx: Exception) {
                                Log.e("OneSignal_Broadcast", "Error en chunk $chunkIndex", chunkEx)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("OneSignal_Broadcast", "Error crítico en broadcast HTTP", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OneSignal_Broadcast", "Error al consultar Firestore", e)
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads a string resource injected via Gradle [resValue].
     * Returns an empty string if the resource does not exist.
     */
    private fun resolveResString(context: Context, resName: String): String {
        val id = context.resources.getIdentifier(resName, "string", context.packageName)
        return if (id != 0) context.getString(id) else ""
    }

    /**
     * Executes an HTTP POST to the OneSignal v1 notifications endpoint.
     * Adds the Authorization header only when [restApiKey] is non-empty.
     *
     * @return HTTP response code.
     */
    private fun postToOneSignal(body: JSONObject, restApiKey: String): Int {
        val url  = URL("https://onesignal.com/api/v1/notifications")
        val conn = url.openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (restApiKey.isNotEmpty()) {
                conn.setRequestProperty("Authorization", "Basic $restApiKey")
            }
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val code = conn.responseCode

            // Log the error body to understand API rejections
            if (code !in 200..299) {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorBody = BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                Log.e("OneSignal_HTTP", "Respuesta de error (HTTP $code): $errorBody")
            }

            code
        } finally {
            conn.disconnect()
        }
    }
}

