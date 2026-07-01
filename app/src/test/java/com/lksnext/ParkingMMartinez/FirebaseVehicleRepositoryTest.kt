package com.lksnext.ParkingMMartinez

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.lksnext.ParkingMMartinez.data.repository.FirebaseVehicleRepository
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

class FirebaseVehicleRepositoryTest {

    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    private lateinit var repository: FirebaseVehicleRepository

    @Before
    fun setUp() {
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockFirestore.collection("vehicles")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        repository = FirebaseVehicleRepository()
    }

    @After
    fun tearDown() {
        mockedFirestoreStatic.close()
    }

    @Test
    fun getVehicles_returnsEmptyListOnError() = runBlocking {
        val mockQuery = mock(Query::class.java)
        `when`(mockCollection.whereEqualTo("userId", "user123")).thenReturn(mockQuery)

        val task: Task<QuerySnapshot> = Tasks.forException(Exception("Firebase error"))
        `when`(mockQuery.get()).thenReturn(task)

        val result = repository.getVehicles("user123")

        assertTrue(result.isEmpty())
    }

    @Test
    fun addVehicle_succeeds() {
        runBlocking {
            val vehicle = Vehicle("v1", "user123", "Car", "1234ABC", VehicleType.STANDARD)
            val task: Task<Void> = Tasks.forResult(null)

            `when`(mockCollection.document("v1")).thenReturn(mockDocument)
            `when`(mockDocument.set(any())).thenReturn(task)

            // Verificamos que no lance excepción
            repository.addVehicle("user123", vehicle)
            verify(mockDocument).set(any())
        }
    }

    @Test
    fun deleteVehicle_succeeds() {
        runBlocking {
            val vehicle = Vehicle("v1", "user123", "Car", "1234ABC", VehicleType.STANDARD)
            val task: Task<Void> = Tasks.forResult(null)

            `when`(mockCollection.document("v1")).thenReturn(mockDocument)
            `when`(mockDocument.delete()).thenReturn(task)

            repository.deleteVehicle("user123", vehicle)
            verify(mockDocument).delete()
        }
    }
}

