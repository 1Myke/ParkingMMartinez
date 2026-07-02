package com.lksnext.ParkingMMartinez

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.lksnext.ParkingMMartinez.ui.navigation.LksNavigation
import com.lksnext.ParkingMMartinez.ui.theme.LksParkingTheme
import com.onesignal.OneSignal
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fetchFirebaseToken()

        setContent {
            LksParkingTheme {
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> /* Feedback opcional */ }

                LaunchedEffect(Unit) {
                    delay(500)

                    checkAndRequestAndroidPermissions(context) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    initializeOneSignal(context)
                }

                LksNavigation()
            }
        }
    }

    private fun fetchFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_ERROR", "No se pudo obtener el token de Firebase", task.exception)
                return@addOnCompleteListener
            }
            Log.d("FCM_SUCCESS", "¡Dispositivo registrado con éxito! Token: ${task.result}")
        }
    }

    private fun checkAndRequestAndroidPermissions(context: Context, onLaunchRequest: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                onLaunchRequest()
            }
        }
    }

    private suspend fun initializeOneSignal(context: Context) {
        val resourceId = context.resources.getIdentifier(
            "onesignal_app_id_secret", "string", context.packageName
        )
        val appId = if (resourceId != 0) context.getString(resourceId) else ""

        if (appId.isNotEmpty()) {
            OneSignal.initWithContext(context, appId)
            OneSignal.Notifications.requestPermission(true)
        } else {
            Log.e("OneSignal_Error", "App ID vacío en local.properties")
        }
    }
}