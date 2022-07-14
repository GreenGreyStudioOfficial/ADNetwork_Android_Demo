package com.mobileadvsdk.datasource.remote.model

import org.json.JSONObject

internal data class AppInfoRemote(
    val id: String,
    val name: String,
    val bundle: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("bundle", bundle)
    }
}
