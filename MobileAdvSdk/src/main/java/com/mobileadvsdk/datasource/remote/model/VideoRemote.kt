package com.mobileadvsdk.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONArray
import org.json.JSONObject
@Keep
internal data class VideoRemote(
    val mimes: List<String>,
    val w: Int,
    val h: Int,
    val ext: ExtRemote
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("mimes", JSONArray().apply {
            mimes.forEach { put(it) }
        })
        put("w", w)
        put("h", h)
        put("ext", ext.toJson())
    }
}

internal data class BannerRemote(
    val mimes: List<String>,
    val w: Int,
    val h: Int,
    val ext: ExtRemote,
    val api: List<Int>,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("mimes", JSONArray().apply {
            mimes.forEach { put(it) }
        })
        put("w", w)
        put("h", h)
        put("ext", ext.toJson())
        put("api", JSONArray().apply {
            api.forEach { put(it) }
        })
    }
}
