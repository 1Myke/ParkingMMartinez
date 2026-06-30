package com.lksnext.ParkingMMartinez

import android.app.Application
import android.util.Log
import com.onesignal.OneSignal

class OneSignalInit : Application() {
    override fun onCreate() {
        super.onCreate()

        val resourceId = resources.getIdentifier("onesignal_app_id_secret", "string", packageName)
        val appId = if (resourceId != 0) getString(resourceId) else ""

        if (appId.isNotEmpty()) {
            OneSignal.initWithContext(this, appId)
            Log.d("OneSignal_Success", "¡OneSignal inicializado correctamente en segundo plano!")
        } else {
            Log.e("OneSignal_Error", "¡El App ID de OneSignal está vacío! Verifica local.properties")
        }
    }
}
