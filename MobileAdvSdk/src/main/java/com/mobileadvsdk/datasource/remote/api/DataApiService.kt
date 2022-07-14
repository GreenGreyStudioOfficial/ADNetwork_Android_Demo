package com.mobileadvsdk.datasource.remote.api

import android.util.Log
import com.mobileadvsdk.datasource.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resumeWithException

private const val OKHTTP_CONNECT_TIMEOUT_MS = 20_000
private const val OKHTTP_READ_TIMEOUT_MS = 20_000

object DataApiServiceImpl {
    fun getUrl(url: String): Flow<Unit> {
        return flow {
            loadUrl(url)
            emit(Unit)
        }
    }

    internal fun loadStartData(data: AdvDataRequestRemote, key: String = "secret"): Flow<AdvDataRemote> = flow {
        val res = loadAvdData(key, data)
        emit(res)
    }

    private suspend fun loadAvdData(key: String, data: AdvDataRequestRemote): AdvDataRemote =
        suspendCancellableCoroutine {
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
                it.resumeWithException(t)
                urlConnection.disconnect()
            }

            try {
                val json = urlConnection.inputStream.bufferedReader().readText()
                it.resume(json.toAdvDataRemote()) {
                    urlConnection.disconnect()
                }
            } catch (t: Throwable) {
                it.resumeWithException(t)
            } finally {
                urlConnection.disconnect()
            }
        }

    private suspend fun loadUrl(host: String) : Unit {
        suspendCancellableCoroutine<Unit> {
            val url = URL(host)

            val urlConnection = (url.openConnection() as HttpURLConnection).apply {
                readTimeout = OKHTTP_READ_TIMEOUT_MS
                connectTimeout = OKHTTP_CONNECT_TIMEOUT_MS
            }

            try {
                urlConnection.inputStream.bufferedReader().readText()
                it.resume(Unit) {
                    urlConnection.disconnect()
                }
            } catch (t: Throwable) {
                it.resumeWithException(t)
            } finally {
                urlConnection.disconnect()
            }
        }
    }

}


private fun JSONObject.getStringOrNull(key: String): String? =
    if (isNull(key)) null else getString(key)

private fun JSONObject.getLongOrNull(key: String): Long? = if (isNull(key)) null else getLong(key)
private fun JSONObject.getJsonObjectOrNull(key: String): JSONObject? = if (isNull(key)) null else getJSONObject(key)
private fun JSONObject.getJsonArrayOrNull(key: String): JSONArray? = if (isNull(key)) null else getJSONArray(key)


private fun String.toAdvDataRemote(): AdvDataRemote {
    val json = JSONObject(this)
    val id = json.getStringOrNull("id")
    val bidid = json.getStringOrNull("bidid")
    val arr = json.getJsonArrayOrNull("seatbid")
    val list = mutableListOf<SeatbidRemote>()
    arr?.let {
        for (i in 0 until it.length()) {
            val str = it[i]
            list.add(str.toString().toSeatbidRemote())
        }
    }

    return AdvDataRemote(id, bidid, list)
}


private fun String.toSeatbidRemote(): SeatbidRemote = JSONObject(this)
    .run {
        val arr = getJsonArrayOrNull("bid")
        val list = mutableListOf<BidRemote>()
        arr?.let {
            for (i in 0 until it.length()) {
                val str = it[i].toString()
                list.add(str.toBidRemote())
            }
        }
        SeatbidRemote(list)
    }

private fun String.toBidRemote(): BidRemote = JSONObject(this)
    .run {
        val id = getStringOrNull("id")
        val impid = getStringOrNull("impid")
        val nurl = getStringOrNull("nurl")
        val lurl = getStringOrNull("lurl")
        val adm = getStringOrNull("adm")
        val cid = getStringOrNull("cid")
        val crid = getStringOrNull("crid")
        val extAdv = getJsonObjectOrNull("ext")?.toString()?.toExtAdvRemote()
        BidRemote(id, impid, nurl, lurl, adm, cid, crid, extAdv)
    }

private fun String.toExtAdvRemote(): ExtAdvRemote = JSONObject(this)
    .run {
        val cache_max = getLongOrNull("cache_max")
        val cache_timeout = getLongOrNull("cache_timeout")
        val req_timeout = getLongOrNull("req_timeout")
        val imp_timeout = getLongOrNull("imp_timeout")
        ExtAdvRemote(cache_max, cache_timeout, req_timeout, imp_timeout)
    }
