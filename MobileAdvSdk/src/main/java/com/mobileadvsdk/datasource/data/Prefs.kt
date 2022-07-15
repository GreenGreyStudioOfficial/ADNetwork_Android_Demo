package com.mobileadvsdk.datasource.data

import android.content.Context
import com.mobileadvsdk.AdvSDK
import java.util.*


internal object Prefs {
    private const val USER_ID_KEY = "USER_ID"
    private val prefs = AdvSDK.context.getSharedPreferences("com.mobileadvsdk", Context.MODE_PRIVATE)
    val userId: String
        get() = prefs.getString(USER_ID_KEY, null) ?: run {
            val uuid = UUID.randomUUID().toString()
            prefs.edit().apply {
                putString(USER_ID_KEY, uuid)
                apply()
            }
            uuid
        }
}