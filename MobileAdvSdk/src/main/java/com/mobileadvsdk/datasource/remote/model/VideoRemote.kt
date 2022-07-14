package com.mobileadvsdk.datasource.remote.model

import org.json.JSONArray
import org.json.JSONObject

internal data class VideoRemote(
    val mimes: List<String>?,
    val w: Int,
    val h: Int,
    val ext: ExtRemote
) {
    fun toJson(): JSONObject = JSONObject().apply {
        mimes?.let { list ->
            put("mimes", JSONArray().apply {
                list.forEach { put(it) }
            })
        }
        put("w",w)
        put("h",h)
        put("ext",ext.toJson())
    }
}
