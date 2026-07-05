package com.lksnext.ParkingMMartinez

import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.model.Reservation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class BookingRepositoryTest {
    @Test
    fun testDefaultMethods() = runBlocking {
        val repo = object : BookingRepository {
            override suspend fun getAllReservations(): List<Reservation> = emptyList()
            override suspend fun saveReservation(reservation: Reservation) {}
            override suspend fun cancelReservation(reservationId: String) {}
            override suspend fun getUserReservations(userId: String): List<Reservation> = emptyList()
        }

        val (list, version) = repo.getAllReservationsWithVersion()
        assertEquals(0, list.size)
        assertEquals(0L, version)

        val reservation = mock(Reservation::class.java)
        val saved = repo.trySaveReservationAtomic(reservation, 0L)
        assertTrue(saved)
    }
}

