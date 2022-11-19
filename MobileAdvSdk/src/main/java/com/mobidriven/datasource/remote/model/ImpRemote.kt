package com.mobidriven.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
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