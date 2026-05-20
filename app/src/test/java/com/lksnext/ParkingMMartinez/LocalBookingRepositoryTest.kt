package com.lksnext.ParkingMMartinez

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.lksnext.ParkingMMartinez.data.repository.LocalBookingRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.Date

class LocalBookingRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: LocalBookingRepository

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer<LocalTime> { json, _, _ ->
            LocalTime.parse(json.asString)
        })
        .create()

    private val userId = "user_mikel_123"
    private val fakeVehicle = Vehicle(id = userId, name = "Ibiza", plate = "1234XYZ", type = VehicleType.STANDARD, isAdapted = false)
    private val fakeZone = ParkingZone(ZoneNames.STANDARD, 24, 24, 0, Color(0xFF455A64))
    private val fakeReservation = Reservation(
        id = "res_123",
        spotNumber = 12,
        vehicle = fakeVehicle,
        zone = fakeZone,
        date = Date(),
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(9, 0),
        isCheckedIn = false
    )

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        repository = LocalBookingRepository(mockContext)
    }

    @Test
    fun repository_delegatesGetAllReservationsCorrectly() {
        val jsonList = gson.toJson(listOf(fakeReservation))
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(jsonList)

        val result = repository.getAllReservations()

        assertEquals(1, result.size)
        assertEquals("res_123", result.first().id)
    }

    @Test
    fun repository_delegatesSaveReservationSuccessfully() {
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(null)

        repository.saveReservation(fakeReservation)

        verify(mockEditor).apply()
    }

    @Test
    fun repository_delegatesCancelReservationSuccessfully() {
        val jsonList = gson.toJson(listOf(fakeReservation))
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(jsonList)

        repository.cancelReservation("res_123")

        verify(mockEditor).apply()
    }
}