package com.mobileadvsdk.datasource.remote.model

import org.json.JSONObject

internal data class ExtRemote(
    val rewarded: Long,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("rewarded", rewarded)
    }
}
