package com.lksnext.ParkingMMartinez

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.lksnext.ParkingMMartinez.ui.navigation.LksNavigation
import com.lksnext.ParkingMMartinez.ui.theme.LksParkingTheme
import com.onesignal.OneSignal
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LksParkingTheme {
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> /* Feedback opcional */ }

                LaunchedEffect(Unit) {
                    // Evitamos que el diálogo interrumpa el inflado inicial de las vistas
                    delay(500)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // === SOLUCIÓN AL CRASH EXCLUSIVA PARA TU ARCHIVO ===
                    // Forzamos la lectura del ID antes de pedirle nada a OneSignal
                    val resourceId = context.resources.getIdentifier("onesignal_app_id_secret", "string", context.packageName)
                    val appId = if (resourceId != 0) context.getString(resourceId) else ""

                    if (appId.isNotEmpty()) {
                        // Aseguramos que esté inicializado en este punto
                        OneSignal.initWithContext(context, appId)
                        // Ahora sí se puede pedir de forma segura
                        OneSignal.Notifications.requestPermission(true)
                    } else {
                        android.util.Log.e("OneSignal_Error", "App ID vacío en local.properties")
                    }
                }

                // Conservamos intacta tu petición del Token de tu servicio de Firebase
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        android.util.Log.e("FCM_ERROR", "No se pudo obtener el token de Firebase", task.exception)
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    android.util.Log.d("FCM_SUCCESS", "¡Dispositivo registrado con éxito! Token: $token")
                }

                LksNavigation()
            }
        }
    }
}
