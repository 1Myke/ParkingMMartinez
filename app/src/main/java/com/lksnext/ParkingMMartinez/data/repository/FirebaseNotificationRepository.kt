package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.lksnext.ParkingMMartinez.model.NotificationItem
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseNotificationRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository {

    // ── Supported locale codes for multi-language push payloads ──────────────
    private val SUPPORTED_LOCALES = listOf("en", "es", "de", "fr", "it", "pt", "eu")
    private val ISO_DATE_PATTERN  = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    /** Lightweight projection of a Firestore user document. */
    private data class UserEntry(
        val userId: String,
        val onesignalId: String,
        val languageCode: String
    )

    private val firestore  = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("notifications")

    // ── Standard notification CRUD ────────────────────────────────────────────

    override fun saveNotification(notification: NotificationItem) {
        val docRef = collection.document()
        docRef.set(notification.copy(id = docRef.id))
    }

    override fun getNotificationsFlow(userId: String, onUpdate: (List<NotificationItem>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) { onUpdate(emptyList()); return@addSnapshotListener }
                onUpdate(snapshot.toObjects(NotificationItem::class.java)
                    .sortedByDescending { it.timestamp?.time ?: 0L })
            }
    }

    override fun markAllAsRead(userId: String) {
        collection.whereEqualTo("userId", userId).whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.update(it.reference, "isRead", true) }
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
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
            }
    }

    // ── Device ↔ user wiring ─────────────────────────────────────────────────

    override fun linkDeviceWithUser(userId: String) {
        val immediateId = OneSignal.User.pushSubscription.id
        if (!immediateId.isNullOrEmpty()) {
            saveOneSignalIdToFirestore(userId, immediateId)
            return
        }
        Log.d(TAG_FIRESTORE, "Subscription ID no disponible aún para user=$userId. Registrando observador...")
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

    private fun saveOneSignalIdToFirestore(userId: String, onesignalId: String) {
        Log.d(TAG_FIRESTORE, "Guardando onesignal_id='$onesignalId' para user='$userId'…")
        firestore.collection("users").document(userId)
            .set(mapOf("onesignal_id" to onesignalId), SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG_FIRESTORE, "✅ onesignal_id guardado para user='$userId'") }
            .addOnFailureListener { e -> Log.e(TAG_FIRESTORE, "❌ Error guardando onesignal_id", e) }
    }

    // ── Single-user push ──────────────────────────────────────────────────────

    override fun sendPushNotification(context: Context, targetUserId: String, title: String, message: String) {
        firestore.collection("users").document(targetUserId).get()
            .addOnSuccessListener { document ->
                val targetId = document.getString("onesignal_id") ?: return@addOnSuccessListener
                if (targetId.isBlank()) return@addOnSuccessListener
                CoroutineScope(ioDispatcher).launch {
                    try {
                        val appId   = resolveResString(context, "onesignal_app_id_secret")
                        val restKey = resolveResString(context, "onesignal_rest_api_key_secret")
                        if (appId.isEmpty()) { Log.e(TAG_PUSH, "App ID vacío — push cancelado."); return@launch }
                        val body = JSONObject().apply {
                            put("app_id", appId)
                            put("include_subscription_ids", JSONArray().put(targetId))
                            put("headings", JSONObject().put("en", title))
                            put("contents", JSONObject().put("en", message))
                            put("android_sound", "notification")
                            put("priority", 10)
                        }
                        val code = postToOneSignal(body, restKey)
                        Log.d(TAG_PUSH, "Resultado envío: código $code")
                    } catch (e: Exception) {
                        Log.e(TAG_PUSH, "Error crítico al enviar push", e)
                    }
                }
            }
    }

    // ── Broadcast push (Issues 1 + 3) ────────────────────────────────────────
    //
    // Refactored into small private methods (< 15 cognitive complexity each) and
    // extended with:
    //   • Multi-language OneSignal payload — each device receives the text in its
    //     language (set via OneSignal.User.setLanguage when the user changes locale).
    //   • Per-user Firestore NotificationItem saved in the user's stored language.
    //   • ISO-8601 date args ("yyyy-MM-dd") are formatted per locale automatically.

    override fun sendBroadcastNotification(
        context: Context,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    ) {
        Log.d(TAG_BROADCAST, "sendBroadcastNotification() llamado. Consultando Firestore…")
        firestore.collection("users").get()
            .addOnSuccessListener { handleBroadcastUsers(context, it, titleResId, bodyResId, bodyFormatArgs) }
            .addOnFailureListener { e -> Log.e(TAG_BROADCAST, "Error al consultar Firestore", e) }
    }

    private fun handleBroadcastUsers(
        context: Context,
        snapshot: QuerySnapshot,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    ) {
        val users = collectUsersWithSubscription(snapshot)
        Log.d(TAG_BROADCAST, "Firestore: ${snapshot.size()} docs, ${users.size} con onesignal_id válido.")
        if (users.isEmpty()) {
            Log.e(TAG_BROADCAST, "❌ Ningún usuario tiene onesignal_id. Verifica linkDeviceWithUser().")
            return
        }
        CoroutineScope(ioDispatcher).launch {
            executeBroadcast(context, users, titleResId, bodyResId, bodyFormatArgs)
        }
    }

    private fun collectUsersWithSubscription(snapshot: QuerySnapshot): List<UserEntry> =
        snapshot.documents.mapNotNull { doc ->
            val id = doc.getString("onesignal_id")
            if (id.isNullOrBlank()) null
            else UserEntry(
                userId       = doc.id,
                onesignalId  = id,
                languageCode = doc.getString("language") ?: "en"
            )
        }

    private fun executeBroadcast(
        context: Context,
        users: List<UserEntry>,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    ) {
        val appId   = resolveResString(context, "onesignal_app_id_secret")
        val restKey = resolveResString(context, "onesignal_rest_api_key_secret")
        if (!validateBroadcastCredentials(appId, restKey)) return

        val (headings, contents) = buildMultiLanguagePayload(context, titleResId, bodyResId, bodyFormatArgs)
        val succeeded = sendAllChunks(users.map { it.onesignalId }, appId, headings, contents, restKey)
        if (succeeded) persistNotificationsToFirestore(context, users, titleResId, bodyResId, bodyFormatArgs)
    }

    private fun validateBroadcastCredentials(appId: String, restKey: String): Boolean {
        if (appId.isEmpty()) { Log.e(TAG_BROADCAST, "❌ App ID vacío — broadcast cancelado."); return false }
        if (restKey.isEmpty()) Log.e(TAG_BROADCAST, "❌ REST API Key vacía — el servidor rechazará la petición.")
        return true
    }

    /** Builds OneSignal headings/contents JSONObjects with all supported translations. */
    private fun buildMultiLanguagePayload(
        context: Context,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    ): Pair<JSONObject, JSONObject> {
        val headings = JSONObject()
        val contents = JSONObject()
        SUPPORTED_LOCALES.forEach { code ->
            headings.put(code, resolveLocalizedString(context, code, titleResId, emptyList()))
            contents.put(code, resolveLocalizedString(context, code, bodyResId, bodyFormatArgs))
        }
        return headings to contents
    }

    /**
     * Resolves [resId] in the given [localeCode], optionally substituting [args].
     * ISO-8601 date strings in [args] are reformatted in [localeCode]'s locale.
     */
    private fun resolveLocalizedString(
        context: Context,
        localeCode: String,
        resId: Int,
        args: List<String>
    ): String {
        val locale = Locale.forLanguageTag(localeCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val ctx          = context.createConfigurationContext(config)
        val resolvedArgs = args.map { formatArgForLocale(it, locale) }.toTypedArray()
        return if (resolvedArgs.isEmpty()) ctx.getString(resId) else ctx.getString(resId, *resolvedArgs)
    }

    /** If [arg] is an ISO-8601 date ("yyyy-MM-dd"), returns it formatted for [locale]. */
    private fun formatArgForLocale(arg: String, locale: Locale): String {
        if (!ISO_DATE_PATTERN.matches(arg)) return arg
        return try {
            val raw = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(arg)
            if (raw != null) SimpleDateFormat("EEEE, MMM d", locale).format(raw) else arg
        } catch (e: Exception) { arg }
    }

    private fun sendAllChunks(
        ids: List<String>,
        appId: String,
        headings: JSONObject,
        contents: JSONObject,
        restKey: String
    ): Boolean {
        var atLeastOneSucceeded = false
        ids.chunked(2_000).forEachIndexed { index, chunk ->
            try {
                val code = postToOneSignal(buildPushPayload(appId, chunk, headings, contents), restKey)
                Log.d(TAG_BROADCAST, "Chunk $index → HTTP $code")
                if (code in 200..299) { atLeastOneSucceeded = true; Log.d(TAG_BROADCAST, "✅ Chunk $index OK.") }
                else Log.e(TAG_BROADCAST, "❌ Chunk $index rechazado (HTTP $code).")
            } catch (e: Exception) { Log.e(TAG_BROADCAST, "Error en chunk $index", e) }
        }
        return atLeastOneSucceeded
    }

    private fun buildPushPayload(appId: String, ids: List<String>, headings: JSONObject, contents: JSONObject) =
        JSONObject().apply {
            put("app_id", appId)
            put("include_subscription_ids", JSONArray().apply { ids.forEach { put(it) } })
            put("headings", headings)
            put("contents", contents)
            put("android_sound", "notification")
            put("priority", 10)
        }

    /** Saves a [NotificationItem] per user, translated into the user's stored language. */
    private fun persistNotificationsToFirestore(
        context: Context,
        users: List<UserEntry>,
        titleResId: Int,
        bodyResId: Int,
        bodyFormatArgs: List<String>
    ) {
        users.forEach { user ->
            val localTitle = resolveLocalizedString(context, user.languageCode, titleResId, emptyList())
            val localBody  = resolveLocalizedString(context, user.languageCode, bodyResId, bodyFormatArgs)
            saveNotification(NotificationItem(userId = user.userId, title = localTitle, body = localBody, isRead = false))
        }
        Log.d(TAG_BROADCAST, "✅ NotificationItem guardado para ${users.size} usuarios.")
    }

    // ── HTTP helper ───────────────────────────────────────────────────────────

    private fun resolveResString(context: Context, resName: String): String {
        val id = context.resources.getIdentifier(resName, "string", context.packageName)
        return if (id != 0) context.getString(id) else ""
    }

    private fun postToOneSignal(body: JSONObject, restApiKey: String): Int {
        val conn = URL("https://onesignal.com/api/v1/notifications").openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            if (restApiKey.isNotEmpty()) conn.setRequestProperty("Authorization", "Basic $restApiKey")
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()); it.flush() }
            val code = conn.responseCode
            if (code !in 200..299) {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                Log.e(TAG_HTTP, "HTTP $code: $err")
            }
            code
        } finally { conn.disconnect() }
    }

    companion object {
        private const val TAG_BROADCAST = "OneSignal_Broadcast"
        private const val TAG_PUSH      = "OneSignal_Push"
        private const val TAG_FIRESTORE = "OneSignal_Firestore"
        private const val TAG_HTTP      = "OneSignal_HTTP"
    }
}

