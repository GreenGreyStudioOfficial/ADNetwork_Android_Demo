package com.mobileadvsdk.datasource.remote.model

import com.mobileadvsdk.BuildConfig
import org.json.JSONObject

internal data class ImpRemote(
    val id: String,
    val video: VideoRemote?,
    val banner: BannerRemote?,
    val instl: Int,
    val displaymanager: String ,
    val displaymanagerver: String
) {
    fun toJson() : JSONObject  = JSONObject().apply {
        put("id", id)
        video?.let { put("video", it.toJson()) }
        banner?.let { put("banner", it.toJson()) }
        put("instl", instl)
        put("displaymanager", displaymanager)
        put("displaymanagerver", displaymanagerver)
    }
}