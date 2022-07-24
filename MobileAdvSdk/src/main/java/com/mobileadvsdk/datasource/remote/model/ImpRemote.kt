package com.mobileadvsdk.datasource.remote.model

import org.json.JSONObject

internal data class ImpRemote(
    val id: String,
    val video: VideoRemote?,
    val banner: BannerRemote?,
    val instl: Int
) {
    fun toJson() : JSONObject  = JSONObject().apply {
        put("id", id)
        video?.let { put("video", it.toJson()) }
        banner?.let { put("banner", it.toJson()) }
        put("instl", instl)
    }
}