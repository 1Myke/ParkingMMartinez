package com.lksnext.ParkingMMartinez

import com.lksnext.ParkingMMartinez.data.ParkingManager
import com.lksnext.ParkingMMartinez.model.VehicleType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParkingManagerTest {

    // Esto se ejecuta AUTOMÁTICAMENTE antes de cada @Test
    @Before
    fun setUp() {
        // Reiniciamos todas las plazas a "libres" usando syncWithReservations con una lista vacía
        ParkingManager.syncWithReservations(emptyList())
    }

    @Test
    fun getAvailableSpotsCount_whenInitial_returnsTotalSpots() {
        // Arrange (Preparar) - El parking está limpio gracias al @Before

        // Act (Actuar) - Contamos las plazas de motos (debería haber 16 según tu mock)
        val availableMotorcycles = ParkingManager.getAvailableSpotsCount(VehicleType.MOTORCYCLE)
        val totalMotorcycles = ParkingManager.getTotalSpotsCount(VehicleType.MOTORCYCLE)

        // Assert (Verificar)
        assertEquals(16, totalMotorcycles)
        assertEquals(16, availableMotorcycles)
    }

    @Test
    fun occupyFirstAvailableSpot_whenSpotAvailable_occupiesAndDecrementsCount() {
        // Arrange
        val type = VehicleType.ADAPTED // Inicialmente hay 6 plazas libres

        // Act
        val occupiedSpotNumber = ParkingManager.occupyFirstAvailableSpot(type)
        val remainingSpots = ParkingManager.getAvailableSpotsCount(type)

        // Assert
        assertNotNull(occupiedSpotNumber)
        assertEquals(1, occupiedSpotNumber) // Debería tomar la primera (id 1)
        assertEquals(5, remainingSpots) // Quedan 5 de las 6 originales
    }

    @Test
    fun occupyFirstAvailableSpot_whenNoSpotsAvailable_returnsNull() {
        // Arrange - Ocupamos las 4 plazas eléctricas que existen en tu mock
        repeat(4) {
            ParkingManager.occupyFirstAvailableSpot(VehicleType.ELECTRIC)
        }

        // Act - Intentamos ocupar una quinta plaza eléctrica
        val extraSpotNumber = ParkingManager.occupyFirstAvailableSpot(VehicleType.ELECTRIC)

        // Assert
        assertNull(extraSpotNumber)
    }

    @Test
    fun releaseSpot_whenSpotWasOccupied_freesTheSpot() {
        // Arrange
        val type = VehicleType.STANDARD
        val spotNumber = ParkingManager.occupyFirstAvailableSpot(type) // Ocupa la número 11
        assertNotNull(spotNumber)

        // Act
        ParkingManager.releaseSpot(spotNumber!!)
        val finalAvailableCount = ParkingManager.getAvailableSpotsCount(type)

        // Assert
        assertEquals(24, finalAvailableCount) // Vuelve a estar el lote completo de 24 libre
    }
}