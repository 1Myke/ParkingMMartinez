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
import com.google.firebase.firestore.getField

class FirebaseBookingRepositoryTest {

    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference
    private lateinit var mockMetadataCollection: CollectionReference
    private lateinit var mockLockDoc: DocumentReference

    private lateinit var repository: FirebaseBookingRepository

    @Before
    fun setUp() {
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)
        mockMetadataCollection = mock(CollectionReference::class.java)
        mockLockDoc = mock(DocumentReference::class.java)

        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockFirestore.collection("bookings")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        `when`(mockFirestore.collection("metadata")).thenReturn(mockMetadataCollection)
        `when`(mockMetadataCollection.document("booking_lock")).thenReturn(mockLockDoc)

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

    @Test
    fun getAllReservations_returnsEmptyList_whenNoDocuments() = runBlocking {
        val mockQuerySnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
        `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
        val task: Task<com.google.firebase.firestore.QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
        `when`(mockCollection.get()).thenReturn(task)

        val result = repository.getAllReservations()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAllReservationsWithVersion_returnsEmptyListAndVersion() = runBlocking {
        val mockQuerySnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
        `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
        val queryTask: Task<com.google.firebase.firestore.QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
        `when`(mockCollection.get()).thenReturn(queryTask)

        val mockDocSnapshot = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
        `when`(mockDocSnapshot.getLong("version")).thenReturn(5L)
        val docTask: Task<com.google.firebase.firestore.DocumentSnapshot> = Tasks.forResult(mockDocSnapshot)
        `when`(mockLockDoc.get()).thenReturn(docTask)

        val (list, version) = repository.getAllReservationsWithVersion()
        assertTrue(list.isEmpty())
        assertEquals(5L, version)
    }

    @Test
    fun getUserReservations_returnsEmptyList_whenNoDocuments() = runBlocking {
        val mockQuery = mock(com.google.firebase.firestore.Query::class.java)
        val mockQuerySnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
        `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
        val task: Task<com.google.firebase.firestore.QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
        
        `when`(mockCollection.whereEqualTo("vehicle.userId", "user1")).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(task)

        val result = repository.getUserReservations("user1")
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun mapDocumentToReservation_returnsReservation_whenValid() = runBlocking {
        val mockQuerySnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
        val mockDocSnapshot = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocSnapshot))
        
        `when`(mockDocSnapshot.getString("id")).thenReturn("res2")
        `when`(mockDocSnapshot.getLong("spotNumber")).thenReturn(5L)
        `when`(mockDocSnapshot.getDate("date")).thenReturn(Date())
        `when`(mockDocSnapshot.getBoolean("isCheckedIn")).thenReturn(true)
        `when`(mockDocSnapshot.getString("startTime")).thenReturn("10:00")
        `when`(mockDocSnapshot.getString("endTime")).thenReturn("12:00")
        
        val vehicle = Vehicle("v1", "user1", "Car", "1234", VehicleType.STANDARD)
        val zone = ParkingZone("Zone A", 10, 10, 0, Color.Red)
        `when`(mockDocSnapshot.getField<Vehicle>("vehicle")).thenReturn(vehicle)
        `when`(mockDocSnapshot.getField<ParkingZone>("zone")).thenReturn(zone)

        val task: Task<com.google.firebase.firestore.QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
        `when`(mockCollection.get()).thenReturn(task)

        val result = repository.getAllReservations()
        assertEquals(1, result.size)
        assertEquals("res2", result[0].id)
    }

    @Test
    fun mapDocumentToReservation_returnsNull_whenExceptionThrown() = runBlocking {
        mockStatic(android.util.Log::class.java).use { mockedLog ->
            mockedLog.`when`<Int> {
                android.util.Log.e(anyString(), anyString())
            }.thenReturn(0)
            
            val mockQuerySnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
            val mockDocSnapshot = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
            `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocSnapshot))
            
            `when`(mockDocSnapshot.getString("id")).thenThrow(RuntimeException("Mock error"))
            
            val task: Task<com.google.firebase.firestore.QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
            `when`(mockCollection.get()).thenReturn(task)

            val result = repository.getAllReservations()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun trySaveReservationAtomic_fails_whenTransactionFails() = runBlocking {
        val vehicle = Vehicle("v1", "user1", "Car", "1234ABC", VehicleType.STANDARD)
        val zone = ParkingZone("Zone A", 10, 10, 0, Color.Red)
        val reservation = Reservation(
            "res1", 1, vehicle, zone, Date(), LocalTime.of(10, 0), LocalTime.of(11, 0), false
        )
        // Without mocking the transaction deeply, it should throw/catch and return false
        val result = repository.trySaveReservationAtomic(reservation, 0L)
        assertFalse(result)
    }
}
