package com.lksnext.ParkingMMartinez

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingMMartinez.data.repository.FirebaseUserRepository
import com.lksnext.ParkingMMartinez.model.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

class FirebaseUserRepositoryTest {

    private lateinit var mockedAuthStatic: MockedStatic<FirebaseAuth>
    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    private lateinit var repository: FirebaseUserRepository

    @Before
    fun setUp() {
        mockAuth = mock(FirebaseAuth::class.java)
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        mockedAuthStatic = mockStatic(FirebaseAuth::class.java)
        mockedAuthStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)

        repository = FirebaseUserRepository()
    }

    @After
    fun tearDown() {
        mockedAuthStatic.close()
        mockedFirestoreStatic.close()
    }

    @Test
    fun authenticate_success() = runBlocking {
        val mockAuthResult = mock(AuthResult::class.java)
        val mockUser = mock(FirebaseUser::class.java)

        `when`(mockUser.uid).thenReturn("user123")
        `when`(mockUser.email).thenReturn("test@test.com")
        `when`(mockAuthResult.user).thenReturn(mockUser)

        val task: Task<AuthResult> = Tasks.forResult(mockAuthResult)
        `when`(mockAuth.signInWithEmailAndPassword("test@test.com", "pass123")).thenReturn(task)

        val result = repository.authenticate("test@test.com", "pass123")

        assertNotNull(result)
        assertEquals("user123", result?.id)
        assertEquals("test@test.com", result?.email)
    }

    @Test
    fun authenticate_failure_returnsNull() = runBlocking {
        val task: Task<AuthResult> = Tasks.forException(Exception("Auth failed"))
        `when`(mockAuth.signInWithEmailAndPassword("wrong@test.com", "wrong")).thenReturn(task)

        val result = repository.authenticate("wrong@test.com", "wrong")

        assertNull(result)
    }

    @Test
    fun getUserById_success() = runBlocking {
        val mockSnapshot = mock(DocumentSnapshot::class.java)
        `when`(mockSnapshot.exists()).thenReturn(true)
        `when`(mockSnapshot.getString("name")).thenReturn("Name")
        `when`(mockSnapshot.getString("lastName")).thenReturn("LastName")
        `when`(mockSnapshot.getString("username")).thenReturn("User")
        `when`(mockSnapshot.getString("email")).thenReturn("user@test.com")
        `when`(mockSnapshot.getString("avatarURL")).thenReturn("http://url")

        val task: Task<DocumentSnapshot> = Tasks.forResult(mockSnapshot)
        `when`(mockDocument.get()).thenReturn(task)

        val result = repository.getUserById("testId")

        assertNotNull(result)
        assertEquals("testId", result?.id)
        assertEquals("Name", result?.name)
    }

    @Test
    fun updateAvatar_success() = runBlocking {
        val task: Task<Void> = Tasks.forResult(null)
        `when`(mockDocument.update("avatarURL", "http://newurl.com")).thenReturn(task)

        val result = repository.updateAvatar("testId", "http://newurl.com")

        assertTrue(result)
    }
}

