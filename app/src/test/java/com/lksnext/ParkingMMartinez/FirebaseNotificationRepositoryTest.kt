package com.lksnext.ParkingMMartinez

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.lksnext.ParkingMMartinez.data.repository.FirebaseNotificationRepository
import com.lksnext.ParkingMMartinez.model.NotificationItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

class FirebaseNotificationRepositoryTest {

    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference
    private lateinit var mockBatch: WriteBatch

    private lateinit var repository: FirebaseNotificationRepository

    @Before
    fun setUp() {
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)
        mockBatch = mock(WriteBatch::class.java)

        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockFirestore.collection("notifications")).thenReturn(mockCollection)
        `when`(mockFirestore.collection("zone_subscriptions")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
        `when`(mockCollection.document()).thenReturn(mockDocument)
        `when`(mockDocument.id).thenReturn("new_id")
        `when`(mockFirestore.batch()).thenReturn(mockBatch)

        repository = FirebaseNotificationRepository()
    }

    @After
    fun tearDown() {
        mockedFirestoreStatic.close()
    }

    @Test
    fun saveNotification_succeeds() {
        val notification = NotificationItem(id = "", title = "Test", body = "", timestamp = null, isRead = false, userId = "user1")
        val task: Task<Void> = Tasks.forResult(null)
        `when`(mockDocument.set(any())).thenReturn(task)

        repository.saveNotification(notification)
        verify(mockDocument).set(any(NotificationItem::class.java))
    }

    @Test
    fun deleteNotification_succeeds() {
        val task: Task<Void> = Tasks.forResult(null)
        `when`(mockCollection.document("notif1")).thenReturn(mockDocument)
        `when`(mockDocument.delete()).thenReturn(task)

        repository.deleteNotification("notif1")
        verify(mockDocument).delete()
    }

    @Test
    fun markAllAsRead_succeeds() {
        val mockQuery = mock(Query::class.java)
        val task = mock(Task::class.java) as Task<QuerySnapshot>

        `when`(mockCollection.whereEqualTo("userId", "user1")).thenReturn(mockQuery)
        `when`(mockQuery.whereEqualTo("isRead", false)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(task)
        // Success listener won't execute automatically unless we use ArgumentCaptor on addOnSuccessListener,
        // but verifying we can call the method without error is valid.

        repository.markAllAsRead("user1")

        verify(mockQuery).get()
        verify(task).addOnSuccessListener(any())
    }

    @Test
    fun deleteAllNotifications_succeeds() {
        val mockQuery = mock(Query::class.java)
        val task = mock(Task::class.java) as Task<QuerySnapshot>

        `when`(mockCollection.whereEqualTo("userId", "user1")).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(task)

        repository.deleteAllNotifications("user1")
        verify(mockQuery).get()
    }
}

