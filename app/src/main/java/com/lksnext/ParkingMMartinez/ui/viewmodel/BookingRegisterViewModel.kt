package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.receiver.BookingAlarmReceiver
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingRegisterViewModel(
    private val repository: BookingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var activeReservations by mutableStateOf(listOf<Reservation>())
        private set

    var pastReservations by mutableStateOf(listOf<Reservation>())
        private set

    var selectedTab by mutableStateOf(0)

    fun loadReservations() {
        val currentUserId = sessionManager.getActiveUserId() ?: return

        viewModelScope.launch {
            val allReservations = repository.getUserReservations(currentUserId)

            val nowMillis = System.currentTimeMillis()

            // 🌟 REDUCCIÓN DE COMPLEJIDAD RADICAL (Bucle e Ifs eliminados)
            // .partition divide automáticamente la colección según la condición
            val (past, active) = allReservations.partition { res ->
                getReservationEndMillis(res) <= nowMillis
            }

            // Ordenamos de manera elegante y eficiente
            activeReservations = active.sortedBy { getReservationStartMillis(it) }
            pastReservations = past.sortedByDescending { getReservationStartMillis(it) }
        }
    }

    /**
     * Devuelve el instante exacto en milisegundos en el que arranca la reserva.
     * Complejidad Sonar: 1
     */
    private fun getReservationStartMillis(res: Reservation): Long {
        return Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.startTime.hour)
            set(Calendar.MINUTE, res.startTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Devuelve el instante exacto en milisegundos en el que termina la reserva.
     * Gestiona limpiamente las reservas que cruzan la medianoche.
     * Complejidad Sonar: 2
     */
    private fun getReservationEndMillis(res: Reservation): Long {
        return Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.endTime.hour)
            set(Calendar.MINUTE, res.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora de fin es numéricamente menor al inicio, cruzó la medianoche (es el día siguiente)
            if (res.endTime.hour < res.startTime.hour) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    fun cancelReservation(context: Context, reservationId: String) {
        viewModelScope.launch {
            repository.cancelReservation(reservationId)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val idAlertaInicio = reservationId.hashCode() + 1
            val idAlertaFin = reservationId.hashCode() + 2

            cancelarAlarmaExistente(context, alarmManager, idAlertaInicio)
            cancelarAlarmaExistente(context, alarmManager, idAlertaFin)

            loadReservations()
        }
    }

    private fun cancelarAlarmaExistente(context: Context, alarmManager: AlarmManager, idAlerta: Int) {
        val intentCancel = Intent(context, BookingAlarmReceiver::class.java)
        val piCancel = PendingIntent.getBroadcast(
            context, idAlerta, intentCancel, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (piCancel != null) {
            alarmManager.cancel(piCancel)
            piCancel.cancel()
        }
    }

    fun doCheckIn(reservationId: String) {
        android.util.Log.d("CHECKIN", "Haciendo check-in de: $reservationId")
    }
}