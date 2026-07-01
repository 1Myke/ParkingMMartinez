package com.lksnext.ParkingMMartinez

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingMMartinez.data.repository.FirebaseBookingRepository
import com.lksnext.ParkingMMartinez.model.ParkingZone
import com.lksnext.ParkingMMartinez.model.Reservation
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import java.time.LocalTime
import java.util.Date

import androidx.compose.ui.graphics.Color

class FirebaseBookingRepositoryTest {

    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    private lateinit var repository: FirebaseBookingRepository

    @Before
    fun setUp() {
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockFirestore.collection("bookings")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        repository = FirebaseBookingRepository()
    }

    @After
    fun tearDown() {
        mockedFirestoreStatic.close()
    }

    @Test
    fun saveReservation_succeeds() {
        runBlocking {
            val vehicle = Vehicle("v1", "user1", "Car", "1234ABC", VehicleType.STANDARD)
            val zone = ParkingZone("Zone A", 10, 10, 0, Color.Red)
            val reservation = Reservation(
                "res1",
                1,
                vehicle,
                zone,
                Date(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                false
            )

            val task: Task<Void> = Tasks.forResult(null)
            `when`(mockCollection.document("res1")).thenReturn(mockDocument)
            `when`(mockDocument.set(any())).thenReturn(task)

            repository.saveReservation(reservation)
            verify(mockDocument).set(any())
        }
    }

    @Test
    fun cancelReservation_succeeds() {
        runBlocking {
            val task: Task<Void> = Tasks.forResult(null)
            `when`(mockCollection.document("res1")).thenReturn(mockDocument)
            `when`(mockDocument.delete()).thenReturn(task)

            repository.cancelReservation("res1")
            verify(mockDocument).delete()
        }
    }
}

