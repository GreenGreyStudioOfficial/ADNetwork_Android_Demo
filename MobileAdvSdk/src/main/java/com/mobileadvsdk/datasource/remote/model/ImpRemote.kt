package com.mobileadvsdk.datasource.remote.model

import org.json.JSONObject

internal data class ImpRemote(
    val id: String,
    val video: VideoRemote,
    val instl: Int
) {
    fun toJson() : JSONObject  = JSONObject().apply {
        put("id", id)
        put("video", video.toJson())
        put("instl", instl)
    }
}