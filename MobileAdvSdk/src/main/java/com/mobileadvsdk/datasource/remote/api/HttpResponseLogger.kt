package com.mobileadvsdk.datasource.remote.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.logging.HttpLoggingInterceptor

class HttpResponseLogger : HttpLoggingInterceptor.Logger {
    private val jsonParser by lazy { JsonParser() }
    private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }

    override fun log(message: String) {
        message.takeIf { it.isNotBlank() }?.let { msg ->
            try {
                Log.d(HttpResponseLogger::class.java.simpleName, gson.toJson(jsonParser.parse(msg)))
            } catch (e: Exception) {
                Log.d(HttpResponseLogger::class.java.simpleName, msg)
            }
        }
    }

}