package com.lksnext.ParkingMMartinez

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import com.lksnext.ParkingMMartinez.data.repository.NeuralNetworkChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class NeuralNetworkChatRepositoryTest {

    @Test
    fun sendFAQ_failsWhenTfliteNotInitialized() = runTest {
        val mockContext = mock(Context::class.java)
        val mockAssets = mock(AssetManager::class.java)
        `when`(mockContext.assets).thenReturn(mockAssets)
        `when`(mockAssets.openFd(anyString())).thenThrow(RuntimeException("Cannot mock tflite file"))

        val repo = NeuralNetworkChatRepository(mockContext, UnconfinedTestDispatcher())

        val result = repo.sendFAQ("Hola")
        assertTrue(result.isFailure)
    }
}

