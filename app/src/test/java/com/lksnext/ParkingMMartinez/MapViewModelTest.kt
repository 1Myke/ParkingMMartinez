package com.lksnext.ParkingMMartinez

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lksnext.ParkingMMartinez.data.repository.BookingRepository
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.ui.viewmodel.MapViewModel
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
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockBookingRepo: BookingRepository
    private lateinit var mockNotificationRepo: NotificationRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockBookingRepo = mock(BookingRepository::class.java)
        mockNotificationRepo = mock(NotificationRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)

        runBlocking {
            `when`(mockBookingRepo.getAllReservations()).thenReturn(emptyList())
        }
        viewModel = MapViewModel(mockBookingRepo, mockNotificationRepo, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialization_generatesDates() {
        assertTrue(viewModel.availableDates.isNotEmpty())
        assertEquals(8, viewModel.availableDates.size)
    }

    @Test
    fun onDateSelected_updatesSelectedDate() {
        val testDate = Date()
        viewModel.onDateSelected(testDate)
        assertEquals(testDate, viewModel.selectedDate)
    }

    @Test
    fun onTimeChange_updatesStartTime() {
        viewModel.onTimeChange(14, 30)
        assertEquals(14, viewModel.selectedStartTime.hour)
        assertEquals(30, viewModel.selectedStartTime.minute)
        assertEquals(15, viewModel.selectedEndTime.hour)
    }
}

