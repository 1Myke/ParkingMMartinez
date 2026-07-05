package com.lksnext.ParkingMMartinez.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingMMartinez.data.ParkingManager
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MapViewModel(
    private val repository: BookingRepository,
    private val notificationRepository: NotificationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var selectedDate by mutableStateOf(Date())
        private set

    var selectedStartTime by mutableStateOf(LocalTime.now().withSecond(0).withNano(0))
        private set

    val selectedEndTime: LocalTime
        get() = selectedStartTime.plusHours(1)

    var zones by mutableStateOf<List<ParkingZone>>(emptyList())
        private set

    var showTimePicker by mutableStateOf(false)

    var availableDates by mutableStateOf<List<Pair<Date, String>>>(emptyList())
        private set

    var selectedDayNumber by mutableStateOf(0)
        private set

    /**
     * Zone names the current user has subscribed to for the currently selected date.
     * Keys are plain zone names (e.g. "EV Charging Zone") — scoped to [selectedDate].
     */
    var subscribedZoneNames by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        val cleanCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDate = cleanCalendar.time

        generateAvailableDates()
        viewModelScope.launch {
            refreshParkingStatus()
        }
        loadUserSubscriptions()
    }

    private fun generateAvailableDates() {
        val list = mutableListOf<Pair<Date, String>>()
        val calendar = Calendar.getInstance()

        selectedDayNumber = calendar.get(Calendar.DAY_OF_MONTH)

        val todayCalendar = calendar.clone() as Calendar
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        list.add(Pair(todayCalendar.time, "TODAY"))

        val dateFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
        for (i in 1..7) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)

            val futureDayCalendar = calendar.clone() as Calendar
            futureDayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            futureDayCalendar.set(Calendar.MINUTE, 0)
            futureDayCalendar.set(Calendar.SECOND, 0)
            futureDayCalendar.set(Calendar.MILLISECOND, 0)

            val label = dateFormat.format(futureDayCalendar.time)
            list.add(Pair(futureDayCalendar.time, label))
        }
        availableDates = list
    }

    fun onDateSelected(fullDate: Date) {
        val calendar = Calendar.getInstance().apply { time = fullDate }
        selectedDayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        selectedDate = fullDate

        viewModelScope.launch {
            refreshParkingStatus()
        }
        loadUserSubscriptions()
    }

    fun onTimeChange(hour: Int, minute: Int) {
        selectedStartTime = LocalTime.of(hour, minute).withSecond(0).withNano(0)
        viewModelScope.launch {
            refreshParkingStatus()
        }
    }

    fun refreshParking() {
        viewModelScope.launch {
            refreshParkingStatus()
        }
        loadUserSubscriptions()
    }

    private suspend fun refreshParkingStatus() {
        try {
            val allBookings = repository.getAllReservations()

            ParkingManager.syncWithReservationsForTimeSlot(
                allBookings = allBookings,
                selectedDate = selectedDate,
                slotStart = selectedStartTime,
                slotEnd = selectedEndTime
            )

            val freshlyCalculatedZones = mutableListOf<ParkingZone>()
            ParkingManager.zones.forEach { zone ->
                freshlyCalculatedZones.add(zone.copy())
            }

            zones = freshlyCalculatedZones

        } catch (e: Exception) {
            android.util.Log.e("MAP_REFRESH_ERROR", "Error refrescando el mapa: ${e.message}")
        }
    }

    // ── Zone-availability bell ─────────────────────────────────────────────────

    private fun currentDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate)

    /** Reload which zones the user has subscribed to for the currently selected date. */
    private fun loadUserSubscriptions() {
        val userId = sessionManager.getActiveUserId()
        if (userId == null) {
            subscribedZoneNames = emptySet()
            return
        }
        notificationRepository.getUserZoneSubscriptions(userId, currentDateKey()) { zoneNames ->
            subscribedZoneNames = zoneNames
        }
    }

    /**
     * Toggles the bell subscription for [zoneName] on the currently selected date.
     * Safe to call from the Compose UI (main thread).
     */
    fun toggleZoneSubscription(context: Context, zoneName: String) {
        val userId       = sessionManager.getActiveUserId() ?: return
        val onesignalId  = OneSignal.User.pushSubscription.id
        val languageCode = context.resources.configuration.locales[0]?.language ?: "en"
        val dateKey      = currentDateKey()

        if (zoneName in subscribedZoneNames) {
            notificationRepository.unsubscribeFromZoneAvailability(userId, zoneName, dateKey)
            subscribedZoneNames = subscribedZoneNames - zoneName
        } else {
            notificationRepository.subscribeToZoneAvailability(
                userId       = userId,
                onesignalId  = onesignalId,
                languageCode = languageCode,
                zoneName     = zoneName,
                dateKey      = dateKey
            )
            subscribedZoneNames = subscribedZoneNames + zoneName
        }
    }
}