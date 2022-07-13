package com.mobileadvsdk.datasource.remote.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resumeWithException

object DataApiServiceImpl {
    fun getUrl(url: String): Flow<Any> {

        return flow { emit(1) }
    }
}

suspend fun urlReq() {
    suspendCancellableCoroutine<Unit> {
        val url = URL("http://www.android.com/")

        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            val res = urlConnection.inputStream.bufferedReader().readText()
//            readStream(ins)
        } catch (t: Throwable) {
            it.resumeWithException(t)
        } finally {
            urlConnection.disconnect()
        }
    }
}
}