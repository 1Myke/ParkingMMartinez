package com.lksnext.ParkingMMartinez

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.lksnext.ParkingMMartinez.data.BookingManager
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.model.ZoneNames
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.Date

class BookingManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var bookingManager: BookingManager

    // Usamos el mismo Gson configurado con LocalTime para preparar datos de prueba
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer<LocalTime> { json, _, _ ->
            LocalTime.parse(json.asString)
        })
        .create()

    private val userId = "user_mikel_123"

    // Objetos de ayuda comunes para las pruebas
    private val fakeVehicle = Vehicle(
        id = "vehicle_001",
        userId = userId,
        name = "Ibiza",
        plate = "1234XYZ",
        type = VehicleType.STANDARD
    )
    private val fakeZone = ParkingZone(ZoneNames.STANDARD, 24, 24, 0, Color(0xFF455A64))
    private val fakeReservation = Reservation(
        id = "res_abc_123",
        spotNumber = 15,
        vehicle = fakeVehicle,
        zone = fakeZone,
        date = Date(),
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(12, 0),
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

        bookingManager = BookingManager(mockContext)
    }

    @Test
    fun getAllBookings_whenEmpty_returnsEmptyList() {
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(null)

        val result = bookingManager.getAllBookings()

        assertTrue(result.isEmpty())
    }

    @Test
    fun saveReservation_persistsReservationCorrectly() {
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(null)

        bookingManager.saveReservation(fakeReservation)

        // Verificamos que se intente guardar un String JSON en la clave correcta
        verify(mockEditor).putString(eq("bookings_list"), anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun getUserBookings_filtersReservationsByVehicleId() {
        val vehicleMikel = Vehicle(
            id = userId,
            userId = userId,
            name = "Ibiza",
            plate = "1234XYZ",
            type = VehicleType.STANDARD
        )

        val vehicleOther = Vehicle(
            id = "other_vehicle_id",
            userId = "other_user",
            name = "Tesla",
            plate = "5555EV",
            type = VehicleType.ELECTRIC
        )

        val reservationMikel = fakeReservation.copy(id = "res_abc_123", vehicle = vehicleMikel)
        val reservationOther = fakeReservation.copy(id = "res_999", vehicle = vehicleOther)

        val jsonList = gson.toJson(listOf(reservationMikel, reservationOther))
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(jsonList)

        // Act
        val result = bookingManager.getUserBookings(userId) // Buscamos por "user_mikel_123"

        // Assert
        assertEquals(1, result.size)
        assertEquals("res_abc_123", result.first().id)
    }

    @Test
    fun cancelReservation_removesTargetIdFromPrefs() {
        // Arrange
        val jsonList = gson.toJson(listOf(fakeReservation))
        `when`(mockPrefs.getString("bookings_list", null)).thenReturn(jsonList)

        // Act
        bookingManager.cancelReservation("res_abc_123")

        // Assert: Al borrar la única que había, debe guardarse una lista vacía "[]"
        val expectedJson = gson.toJson(emptyList<Reservation>())
        verify(mockEditor).putString("bookings_list", expectedJson)
        verify(mockEditor).apply()
    }
}