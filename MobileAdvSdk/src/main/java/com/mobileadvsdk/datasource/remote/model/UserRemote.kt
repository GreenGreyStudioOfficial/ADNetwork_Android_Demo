package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

internal data class UserRemote(
    @SerializedName("id")
    val id: String,
) {
    fun toJson(): JSONObject  = JSONObject().apply {
        put("id",id)
    }
}
