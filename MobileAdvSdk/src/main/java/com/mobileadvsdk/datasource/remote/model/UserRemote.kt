package com.mobileadvsdk.datasource.remote.model

import org.json.JSONObject

internal data class UserRemote(
    val id: String,
) {
    fun toJson(): JSONObject  = JSONObject().apply {
        put("id",id)
    }
}
