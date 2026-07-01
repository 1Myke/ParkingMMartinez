package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.lksnext.ParkingMMartinez.ui.viewmodel.RecoveryViewModel
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
class RecoveryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var viewModel: RecoveryViewModel
    private lateinit var mockedAuthStatic: MockedStatic<FirebaseAuth>

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockAuth = mock(FirebaseAuth::class.java)

        mockedAuthStatic = mockStatic(FirebaseAuth::class.java)
        mockedAuthStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        viewModel = RecoveryViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedAuthStatic.close()
    }

    @Test
    fun validateAndSend_emptyEmail() {
        viewModel.onEmailChange("")
        viewModel.validateAndSend {}
        assertEquals("error_empty_field", viewModel.errorCode)
    }

    @Test
    fun validateAndSend_invalidEmail() {
        viewModel.onEmailChange("invalid")
        viewModel.validateAndSend {}
        assertEquals("error_invalid_email_recovery", viewModel.errorCode)
    }
    
    @Test
    fun clearForm_resetsFields() {
        viewModel.onEmailChange("admin@admin.com")
        viewModel.clearForm()
        assertEquals("", viewModel.email)
        assertNull(viewModel.errorCode)
        assertFalse(viewModel.isLoading)
    }
}

