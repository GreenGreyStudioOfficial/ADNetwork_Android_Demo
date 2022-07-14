package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

internal data class AppInfoRemote(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bundle")
    val bundle: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("bundle", bundle)
    }
}
