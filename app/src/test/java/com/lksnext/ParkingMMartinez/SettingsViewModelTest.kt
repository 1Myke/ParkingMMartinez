package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockUserRepo: UserRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var viewModel: SettingsViewModel

    private lateinit var mockedAuthStatic: MockedStatic<FirebaseAuth>
    private lateinit var mockedFirestoreStatic: MockedStatic<FirebaseFirestore>

    private val userId = "user_123"

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockUserRepo = mock(UserRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        mockAuth = mock(FirebaseAuth::class.java)
        mockFirebaseUser = mock(FirebaseUser::class.java)

        mockedAuthStatic = mockStatic(FirebaseAuth::class.java)
        mockedAuthStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        // SettingsViewModel now holds a FirebaseFirestore instance to persist language
        // preferences. We mock it statically so the constructor doesn't throw
        // IllegalStateException("Default FirebaseApp is not initialized").
        val mockFirestore = mock(FirebaseFirestore::class.java)
        mockedFirestoreStatic = mockStatic(FirebaseFirestore::class.java)
        mockedFirestoreStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)

        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.email).thenReturn("mikel2@lks.com")

        viewModel = SettingsViewModel(mockUserRepo, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedAuthStatic.close()
        mockedFirestoreStatic.close()
    }

    @Test
    fun loadCurrentUserData_populatesUsernameAndEmailFields() {
        val fakeUser = User(
            id = userId,
            name = "Mikel",
            lastName = "Martinez",
            username = "mykol_lks",
            email = "mikel2@lks.com",
            avatarURL = null
        )

        runBlocking {
            `when`(mockUserRepo.getUserById(userId)).thenReturn(fakeUser)
        }

        viewModel.loadCurrentUserData()

        assertEquals("mykol_lks", viewModel.username)
        assertEquals("", viewModel.oldPassword)
    }

    @Test
    fun updateProfile_setsErrorWhenUsernameIsEmpty() {
        viewModel.username = ""

        viewModel.updateProfile()

        assertEquals("error_empty_field", viewModel.errorCode)
        assertNull(viewModel.successCode)
    }

    @Test
    fun clearMessages_resetsErrorAndSuccessCodesToNull() {
        viewModel.errorCode = "error_generic"
        viewModel.successCode = "success_profile_updated"

        viewModel.clearMessages()

        assertNull(viewModel.errorCode)
        assertNull(viewModel.successCode)
    }
}