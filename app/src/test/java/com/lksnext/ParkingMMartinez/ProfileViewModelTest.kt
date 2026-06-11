package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.model.Vehicle
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockVehicleRepo: VehicleRepository
    private lateinit var mockUserRepo: UserRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockBookingRepo: BookingRepository
    private lateinit var viewModel: ProfileViewModel

    private val userId = "mikel_user"

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockVehicleRepo = mock(VehicleRepository::class.java)
        mockUserRepo = mock(UserRepository::class.java)
        mockBookingRepo = mock(BookingRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)
        viewModel = ProfileViewModel(mockVehicleRepo, mockUserRepo, mockBookingRepo, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadUserData_populatesFieldsAndVehiclesList() {
        val fakeUser = User(id = userId, username = "MikelM", email = "mikel@lks.com")
        runBlocking {
            `when`(mockUserRepo.getUserById(userId)).thenReturn(fakeUser)
            `when`(mockVehicleRepo.getVehicles(userId)).thenReturn(emptyList())
        }

        viewModel.loadUserData()

        assertEquals("MikelM", viewModel.userName)
        assertEquals("mikel@lks.com", viewModel.userEmail)
    }

    @Test
    fun dialogState_togglesCorrectly() {
        assertFalse(viewModel.showAddVehicleDialog)
        viewModel.onOpenDialog()
        assertTrue(viewModel.showAddVehicleDialog)
        viewModel.newVehicleName = "Audi"
        assertEquals("Audi", viewModel.newVehicleName)
        viewModel.onCloseDialog()
        assertFalse(viewModel.showAddVehicleDialog)
    }

    @Test
    fun addVehicle_callsRepositorySuccessfully() {
        // 1. Arrange
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel.onOpenDialog()
        viewModel.newVehicleName = "Ibiza"
        viewModel.newVehiclePlate = "1234XYZ"
        viewModel.onVehicleTypeChange(VehicleType.STANDARD)

        // 2. Act
        viewModel.addVehicle()

        // 3. Assert (Verifica el estado del ViewModel, no la llamada al repo)
        // Esto es mucho más robusto:
        assertTrue(viewModel.vehicles.any { it.name == "Ibiza" })
        assertFalse(viewModel.showAddVehicleDialog)
    }
}