package com.lksnext.ParkingMMartinez.data.repository

import android.content.Context
import com.lksnext.ParkingMMartinez.repository.ChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import com.lksnext.ParkingMMartinez.R

class NeuralNetworkChatRepository(
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ChatRepository {

    private var tflite: Interpreter? = null
    private lateinit var vocabulario: List<String>
    private lateinit var respuestas: List<String>

    init {
        try {
            val assetFileDescriptor = context.assets.openFd("parking_corp5.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val modelBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
            tflite = Interpreter(modelBuffer)

            actualizarTextosEIdioma()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun actualizarTextosEIdioma() {
        try {
            vocabulario = context.getString(R.string.bot_vocab)
                .split(",")
                .map { it.trim().lowercase() }

            respuestas = listOf(
                context.getString(R.string.res_clase_0),
                context.getString(R.string.res_clase_1),
                context.getString(R.string.res_clase_2),
                context.getString(R.string.res_clase_3),
                context.getString(R.string.res_clase_4),
                context.getString(R.string.res_clase_5),
                context.getString(R.string.res_clase_6),
                context.getString(R.string.res_clase_7)
            )
        } catch (e: Exception) {
            throw IllegalStateException("Error al cargar los recursos de idioma: ${e.localizedMessage}", e)
        }
    }

    override suspend fun sendFAQ(message: String): Result<String> = withContext(defaultDispatcher) {
        try {
            val interpreter = tflite ?: return@withContext Result.failure(Exception("Modelo TFLite no inicializado"))

            actualizarTextosEIdioma()

            val inputVector = FloatArray(vocabulario.size) { 0f }
            val palabrasUsuario = message.lowercase().replace(Regex("[?,.!¡¿]"), "").split(" ")

            for (palabra in palabrasUsuario) {
                val index = vocabulario.indexOf(palabra)
                if (index != -1) {
                    inputVector[index] = 1f
                }
            }

            if (inputVector.all { it == 0f }) {
                return@withContext Result.success(context.getString(R.string.bot_fallback))
            }

            val inputs = arrayOf(inputVector)
            val outputs = arrayOf(FloatArray(8))

            interpreter.run(inputs, outputs)

            val probabilidades = outputs[0]
            val indiceMaximo = probabilidades.indices.maxByOrNull { probabilidades[it] } ?: 0

            Result.success(respuestas[indiceMaximo])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}