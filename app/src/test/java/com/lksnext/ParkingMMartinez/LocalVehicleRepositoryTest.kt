package com.lksnext.ParkingMMartinez

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lksnext.ParkingMMartinez.data.repository.LocalVehicleRepository
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class LocalVehicleRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: LocalVehicleRepository
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

        repository = LocalVehicleRepository(mockContext)
    }

    @Test
    fun repository_delegatesGetVehiclesToManagerCorrectly() {
        // Corregido con tu modelo
        val testVehicle = Vehicle(id = "moto_1", name = "Honda", plate = "9999CCC", type = VehicleType.MOTORCYCLE, isAdapted = false)
        val jsonList = gson.toJson(listOf(testVehicle))
        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(jsonList)

        val result = repository.getVehicles(userId)

        assertEquals(1, result.size)
        assertEquals("9999CCC", result.first().plate)
    }

    @Test
    fun repository_delegatesAddVehicleSuccessfully() {
        `when`(mockPrefs.getString("vehicles_$userId", null)).thenReturn(null)
        // Corregido con tu modelo
        val vehicle = Vehicle(id = "3", name = "Renault", plate = "4444DDD", type = VehicleType.STANDARD, isAdapted = false)

        repository.addVehicle(userId, vehicle)

        verify(mockEditor).apply()
    }
}