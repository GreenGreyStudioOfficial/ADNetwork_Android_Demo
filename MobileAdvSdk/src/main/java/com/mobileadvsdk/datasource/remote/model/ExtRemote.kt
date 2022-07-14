package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

internal data class ExtRemote(
    @SerializedName("rewarded")
    val rewarded: Long,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("rewarded", rewarded)
    }
}
