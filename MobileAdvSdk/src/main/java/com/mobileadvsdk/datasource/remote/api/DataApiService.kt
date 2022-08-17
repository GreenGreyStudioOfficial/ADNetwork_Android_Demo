package com.mobileadvsdk.datasource.remote.api

import android.util.Log
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import com.mobileadvsdk.toAdvDataRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resumeWithException

internal const val OKHTTP_CONNECT_TIMEOUT_MS = 30_000
internal const val OKHTTP_READ_TIMEOUT_MS = 30_000

internal object DataApiServiceImpl {
    fun getUrl(url: String) {
        AdvSDK.scope.launch(Dispatchers.IO) {
            try {
                loadUrl(url)
            } catch (e: Exception) {
//                Log.e("AdvViewModel", "Error: ${e.localizedMessage}")
            }
        }
    }

    internal fun loadStartData(data: AdvDataRequestRemote, key: String = "secret"): Flow<AdvDataRemote> = flow {
        val res = loadAvdData(key, data)
        emit(res)
        Log.e("DataApiService", "loadStartData thread ${Thread.currentThread().name}")
    }.flowOn(Dispatchers.IO)

    private suspend fun loadAvdData(key: String, data: AdvDataRequestRemote): AdvDataRemote =
        suspendCancellableCoroutine { continuation ->
            val url = URL("https://sp.mobidriven.com/rtb?key=$key")

            val urlConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                readTimeout = OKHTTP_READ_TIMEOUT_MS
                connectTimeout = OKHTTP_CONNECT_TIMEOUT_MS
            }

            try {
                urlConnection.outputStream.use { os ->
                    val input: ByteArray = data.toJson().toString().toByteArray()
                    os.write(input, 0, input.size)
                }
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
                urlConnection.disconnect()
            }

            try {
                urlConnection.inputStream.use { ins ->
                    val json = ins.bufferedReader().readText()
                    continuation.resume(json.toAdvDataRemote()) {
                        urlConnection.disconnect()
                    }
                }
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
            } finally {
                urlConnection.disconnect()
            }
        }

    private suspend fun loadUrl(host: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val url = URL(host)

            val urlConnection = (url.openConnection() as HttpURLConnection).apply {
                readTimeout = OKHTTP_READ_TIMEOUT_MS
                connectTimeout = OKHTTP_CONNECT_TIMEOUT_MS
            }

            try {
                urlConnection.inputStream.use { ins ->
                    ins.bufferedReader().readText()
                    continuation.resume(Unit) {
                        urlConnection.disconnect()
                    }
                }
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
            } finally {
                urlConnection.disconnect()
            }
        }
    }

}
