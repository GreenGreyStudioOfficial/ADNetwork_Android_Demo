package com.mobileadvsdk.datasource.remote.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.logging.HttpLoggingInterceptor

internal class HttpResponseLogger : HttpLoggingInterceptor.Logger {
    private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }

    override fun log(message: String) {
        message.takeIf { it.isNotBlank() }?.let { msg ->
            try {
                Log.d(HttpResponseLogger::class.java.simpleName, gson.toJson(JsonParser.parseString(msg)))
            } catch (e: Exception) {
                Log.d(HttpResponseLogger::class.java.simpleName, msg)
            }
        }
    }

}