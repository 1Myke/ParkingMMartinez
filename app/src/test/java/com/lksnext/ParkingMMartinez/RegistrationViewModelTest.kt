package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.viewmodel.RegistrationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RegistrationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockUserRepo: UserRepository
    private lateinit var mockVehicleRepo: VehicleRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var viewModel: RegistrationViewModel

    private lateinit var mockedAuthStatic: MockedStatic<FirebaseAuth>

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockUserRepo = mock(UserRepository::class.java)
        mockVehicleRepo = mock(VehicleRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        mockAuth = mock(FirebaseAuth::class.java)
        mockFirebaseUser = mock(FirebaseUser::class.java)

        mockedAuthStatic = mockStatic(FirebaseAuth::class.java)
        mockedAuthStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("firebase_uid")

        viewModel = RegistrationViewModel(mockUserRepo, mockVehicleRepo, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedAuthStatic.close()
    }

    @Test
    fun register_invalidEmail() {
        viewModel.onEmailChange("invalidemail")
        viewModel.register {}
        assertEquals(R.string.err_invalid_email, viewModel.errorCode)
    }

    @Test
    fun register_passwordTooShort() {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("123")
        viewModel.register {}
        assertEquals(R.string.err_password_short, viewModel.errorCode)
    }

    @Test
    fun register_passwordsMismatch() {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password")
        viewModel.onPasswordRepeatChange("different")
        viewModel.register {}
        assertEquals(R.string.err_password_mismatch, viewModel.errorCode)
    }

    @Test
    fun clearForm_resetsFields() {
        viewModel.onEmailChange("test")
        viewModel.clearForm()
        assertEquals("", viewModel.email)
        assertEquals(VehicleType.STANDARD, viewModel.selectedVehicleType)
    }
}

