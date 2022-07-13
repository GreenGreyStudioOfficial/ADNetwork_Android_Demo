package com.mobileadvsdk.presentation.player.util

import android.text.TextUtils
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

internal object HttpTools {
    private val TAG = HttpTools::class.java.name

    fun httpGetURL(url: String) {
        if (!TextUtils.isEmpty(url)) {
            object : Thread() {
                override fun run() {
                    var conn: HttpURLConnection? = null
                    try {
                        VASTLog.v(TAG, "connection to URL:$url")
                        val httpUrl = URL(url)
                        HttpURLConnection.setFollowRedirects(true)
                        conn = httpUrl.openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.setRequestProperty("Connection", "close")
                        conn.requestMethod = "GET"
                        conn.connect()
                        val code = conn.responseCode
                        VASTLog.v(TAG, "response code:$code, for URL:$url")
                        conn.inputStream.close()
                        conn.outputStream.close()
                    } catch (e: Exception) {
                        VASTLog.w(TAG, url + ": " + e.message + ":" + e.toString())
                    } finally {
                        conn?.disconnect()
                    }
                }
            }.start()
        } else {
            VASTLog.w(TAG, "url is null or empty")
        }
    }
}