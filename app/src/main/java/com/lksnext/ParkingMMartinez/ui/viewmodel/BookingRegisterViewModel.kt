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

            val (past, active) = allReservations.partition { res ->
                val endPast = getReservationEndMillis(res) <= nowMillis
                val missedCheckIn = !res.isCheckedIn && (getReservationStartMillis(res) + 15 * 60 * 1000 < nowMillis)

                if (missedCheckIn && !res.isCheckedIn) {
                    // Opcional: Aquí podrías disparar un borrado/actualización en background si tu arquitectura lo requiere
                }

                endPast || missedCheckIn
            }

            activeReservations = active.sortedBy { getReservationStartMillis(it) }
            pastReservations = past.sortedByDescending { getReservationStartMillis(it) }
        }
    }

    fun isCheckInWindowActive(res: Reservation): Boolean {
        val now = System.currentTimeMillis()
        val startMillis = getReservationStartMillis(res)
        val windowStart = startMillis - 15 * 60 * 1000
        val windowEnd = startMillis + 15 * 60 * 1000
        return now in windowStart..windowEnd
    }

    fun doCheckIn(reservation: Reservation) {
        viewModelScope.launch {
            val updatedReservation = reservation.copy(isCheckedIn = true)
            repository.saveReservation(updatedReservation) // Actualiza en la base de datos / repo
            loadReservations() // Refresca las listas de la UI
        }
    }

    private fun getReservationStartMillis(res: Reservation): Long {
        return Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.startTime.hour)
            set(Calendar.MINUTE, res.startTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getReservationEndMillis(res: Reservation): Long {
        return Calendar.getInstance().apply {
            time = res.date
            set(Calendar.HOUR_OF_DAY, res.endTime.hour)
            set(Calendar.MINUTE, res.endTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

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
}