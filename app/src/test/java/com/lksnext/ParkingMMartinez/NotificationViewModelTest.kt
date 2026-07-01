package com.lksnext.ParkingMMartinez

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.lksnext.ParkingMMartinez.data.SessionManager
import com.lksnext.ParkingMMartinez.data.repository.NotificationRepository
import com.lksnext.ParkingMMartinez.model.NotificationItem
import com.lksnext.ParkingMMartinez.ui.viewmodel.NotificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    private lateinit var mockRepository: NotificationRepository
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockContext: Context
    private lateinit var mockPowerManager: PowerManager
    private lateinit var viewModel: NotificationViewModel

    private val userId = "mikel_123"
    private val testDispatcher = StandardTestDispatcher()

    private var mockedLooper: org.mockito.MockedStatic<Looper>? = null
    private var mockedHandler: org.mockito.MockedConstruction<Handler>? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock(NotificationRepository::class.java)
        mockSessionManager = mock(SessionManager::class.java)
        mockContext = mock(Context::class.java)
        mockPowerManager = mock(PowerManager::class.java)

        `when`(mockContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockPowerManager)
        `when`(mockContext.packageName).thenReturn("com.lksnext.ParkingMMartinez")
        `when`(mockSessionManager.getActiveUserId()).thenReturn(userId)

        mockedLooper = mockStatic(Looper::class.java)
        val mockLooper = mock(Looper::class.java)
        mockedLooper?.`when`<Looper> { Looper.getMainLooper() }?.thenReturn(mockLooper)

        mockedHandler = mockConstruction(Handler::class.java) { mockHandler, _ ->
            doAnswer { invocation ->
                val runnable = invocation.getArgument<Runnable>(0)
                runnable.run()
                true
            }.`when`(mockHandler).post(any(Runnable::class.java))
        }

        viewModel = NotificationViewModel(mockRepository, mockSessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedHandler?.close()
        mockedLooper?.close()
    }

    // ==========================================
    // 1. TESTS DE INICIALIZACIÓN Y FLUJO DE DATOS
    // ==========================================

    @Test
    fun initViewModel_whenUserLogged_checksBatteryAndLoadsNotifications() = runTest {
        viewModel.initViewModel(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockContext).getSystemService(Context.POWER_SERVICE)
        verify(mockSessionManager, atLeastOnce()).getActiveUserId()
        verify(mockRepository).getNotificationsFlow(eqKotlin(userId), anyKotlin())
    }

    @Test
    fun loadNotifications_whenRepositoryEmitsList_updatesViewModelState() = runTest {
        val fakeList = listOf(mock(NotificationItem::class.java))

        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<NotificationItem>) -> Unit>(1)
            callback.invoke(fakeList)
            null
        }.`when`(mockRepository).getNotificationsFlow(eqKotlin(userId), anyKotlin())

        viewModel.initViewModel(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.notifications.size)
        verify(mockRepository).getNotificationsFlow(eqKotlin(userId), anyKotlin())
    }

    @Test
    fun loadNotifications_whenUserNotLogged_doesNotFetchAndKeepsEmpty() = runTest {
        `when`(mockSessionManager.getActiveUserId()).thenReturn(null)

        viewModel.initViewModel(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.notifications.isEmpty())
        verify(mockRepository, never()).getNotificationsFlow(anyString(), anyKotlin())
    }

    // ==========================================
    // 2. TESTS DE ACCIONES DE NOTIFICACIONES
    // ==========================================

    @Test
    fun markAllAsRead_whenUserLogged_triggersRepository() {
        viewModel.markAllAsRead()
        verify(mockRepository).markAllAsRead(userId)
    }

    @Test
    fun deleteNotification_triggersRepository() {
        val notifId = "notif_abc_123"
        viewModel.deleteNotification(notifId)
        verify(mockRepository).deleteNotification(notifId)
    }

    @Test
    fun deleteAllNotifications_whenUserLogged_triggersRepository() {
        viewModel.deleteAllNotifications()
        verify(mockRepository).deleteAllNotifications(userId)
    }

    @Test
    fun checkBatteryStatus_underDefaultTestEnvironment_setsBatteryOptimizedToFalse() {
        viewModel.checkBatteryStatus(mockContext)
        assertFalse(viewModel.isBatteryOptimized)
    }

    // ==========================================
    // HELPERS PARA MATCHERS EN KOTLIN
    // ==========================================

    // ✅ Evita NPE: si Mockito devuelve null (tipo nullable en Kotlin), usa el valor real
    private fun <T> eqKotlin(value: T): T = org.mockito.Mockito.eq(value) ?: value

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyKotlin(): T = any() as T
}