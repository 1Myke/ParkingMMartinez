package com.lksnext.ParkingMMartinez

import com.lksnext.ParkingMMartinez.data.UserMock
import org.junit.Assert.*
import org.junit.Test

class UserMockTest {

    @Test
    fun userMock_containsDefaultUsers() {
        // Act
        val userList = UserMock.users

        // Assert
        assertTrue(userList.isNotEmpty())
        assertEquals(2, userList.size)

        // Verificamos que el primer usuario sea Mikel
        val firstUser = userList.first()
        assertEquals("mikel_id_123", firstUser.id)
        assertEquals("Mikel", firstUser.name)
        assertEquals("mikel@lksnext.com", firstUser.email)
    }
}