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
                    // Para evitar que el diálogo del sistema interrumpa el proceso de inflado de la app
                    delay(500)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Fuerza la petición del Token para registrar el dispositivo en Google
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