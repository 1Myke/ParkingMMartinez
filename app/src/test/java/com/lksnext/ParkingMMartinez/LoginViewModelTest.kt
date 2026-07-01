package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.ui.viewmodel.LoginViewModel
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
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockUserRepo: UserRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockUserRepo = mock(UserRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        viewModel = LoginViewModel(mockUserRepo, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success() {
        val email = "test@test.com"
        val password = "password"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onRememberMeChange(true)

        var successCalled = false

        runBlocking {
            `when`(mockUserRepo.authenticate(email, password)).thenReturn(
                User(id = "user1", name = "Test", lastName = "User", username = "tester", email = email, pass = password)
            )
        }

        viewModel.login { remember ->
            successCalled = true
            assertTrue(remember)
        }

        assertTrue(successCalled)
        assertNull(viewModel.errorCode)
        verify(mockSessionManager).saveSession(true, "user1")
    }

    @Test
    fun login_failure() {
        val email = "test@test.com"
        val password = "wrongpassword"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)

        var successCalled = false

        runBlocking {
            `when`(mockUserRepo.authenticate(email, password)).thenReturn(null)
        }

        viewModel.login {
            successCalled = true
        }

        assertFalse(successCalled)
        assertEquals("error_invalid_credentials", viewModel.errorCode)
    }

    @Test
    fun resetLoginFields() {
        viewModel.onEmailChange("email")
        viewModel.onPasswordChange("pass")
        viewModel.errorCode = "error"

        viewModel.resetLoginFields()

        assertEquals("", viewModel.email)
        assertEquals("", viewModel.password)
        assertNull(viewModel.errorCode)
    }
}

