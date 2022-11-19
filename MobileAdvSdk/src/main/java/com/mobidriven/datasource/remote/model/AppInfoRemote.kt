package com.mobidriven.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject
@Keep
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
