package com.lksnext.ParkingMMartinez

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lksnext.ParkingMMartinez.data.VehicleManager
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class VehicleManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var vehicleManager: VehicleManager
    private val gson = Gson()
    private val userId = "user_mikel_123"

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)

        vehicleManager = VehicleManager(mockContext)
    }

    @Test
    fun getVehicles_whenNoDataStored_returnsEmptyList() {
        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(null)

        val result = vehicleManager.getVehicles(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun getVehicles_whenDataExists_returnsCorrectParsedList() {
        // Corregido con los parámetros reales de tu data class Vehicle
        val testVehicle = Vehicle(
            id = "car1",
            userId = userId,
            name = "Golf",
            plate = "1234XYZ",
            type = VehicleType.STANDARD
        )
        val jsonList = gson.toJson(listOf(testVehicle))
        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(jsonList)

        val result = vehicleManager.getVehicles(userId)

        assertEquals(1, result.size)
        assertEquals("1234XYZ", result.first().plate)
        assertEquals("Golf", result.first().name)
    }

    @Test
    fun addVehicle_savesUpdatedListInSharedPreferences() {
        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(null)
        val newVehicle = Vehicle(
            id = "car2",
            userId = userId,
            name = "Tesla",
            plate = "5555EV",
            type = VehicleType.ELECTRIC
        )

        vehicleManager.addVehicle(userId, newVehicle)

        verify(mockEditor).putString(eq("vehicles_$userId"), anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun deleteVehicle_removesTargetVehicleAndSavesRemaining() {
        // Corregido añadiendo name e isAdapted
        val car1 = Vehicle(id = "1", userId = userId, name = "Seat Ibiza", plate = "1111AAA", type = VehicleType.STANDARD)
        val car2 = Vehicle(id = "2", userId = userId, name = "Yamaha", plate = "2222BBB", type = VehicleType.MOTORCYCLE)
        val jsonOriginal = gson.toJson(listOf(car1, car2))

        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(jsonOriginal)

        vehicleManager.deleteVehicle(userId, car1)

        val expectedJsonResult = gson.toJson(listOf(car2))
        verify(mockEditor).putString("vehicles_$userId", expectedJsonResult)
        verify(mockEditor).apply()
    }
}