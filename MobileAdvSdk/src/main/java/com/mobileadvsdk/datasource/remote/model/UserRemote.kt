package com.mobileadvsdk.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject
@Keep
internal data class UserRemote(
    val id: String,
) {
    fun toJson(): JSONObject  = JSONObject().apply {
        put("id",id)
    }
}
