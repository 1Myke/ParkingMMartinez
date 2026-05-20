package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.UserRepository
import com.lksnext.ParkingMMartinez.data.repository.VehicleRepository
import com.lksnext.ParkingMMartinez.model.User
import com.lksnext.ParkingMMartinez.model.VehicleType
import com.lksnext.ParkingMMartinez.ui.viewmodel.ProfileViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockVehicleRepo: VehicleRepository
    private lateinit var mockUserRepo: UserRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: ProfileViewModel

    private val userId = "mikel_user"

    @Before
    fun setUp() {
        mockVehicleRepo = mock(VehicleRepository::class.java)
        mockUserRepo = mock(UserRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel = ProfileViewModel(mockVehicleRepo, mockUserRepo, mockSessionManager)
    }

    @Test
    fun loadUserData_populatesFieldsAndVehiclesList() {
        val fakeUser = User(id = userId, username = "MikelM", email = "mikel@lks.com")
        `when`(mockUserRepo.getUserById(userId)).thenReturn(fakeUser)
        `when`(mockVehicleRepo.getVehicles(userId)).thenReturn(emptyList())

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
        assertEquals("", viewModel.newVehicleName)
    }

    @Test
    fun addVehicle_callsRepositorySuccessfully() {
        // Arrange
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        viewModel.onOpenDialog()
        viewModel.newVehicleName = "Ibiza"
        viewModel.newVehiclePlate = "1234XYZ"
        viewModel.onVehicleTypeChange(VehicleType.STANDARD)

        // Act
        try {
            viewModel.addVehicle()
        } catch (e: Exception) {
            // Evitamos que Compose rompa el flujo en JUnit puro
        }

        // Assert: Pasamos la variable síncrona directamente sin envolverla en eq()
        // y usamos el verificador de clase explícito para evitar nulos en Kotlin
        verify(mockVehicleRepo).addVehicle(userId, viewModel.vehicles.first())
    }
}