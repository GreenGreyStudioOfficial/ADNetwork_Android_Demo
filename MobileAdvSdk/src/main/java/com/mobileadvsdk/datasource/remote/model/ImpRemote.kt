package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

internal data class ImpRemote(

    @SerializedName("id")
    val id: String,

    @SerializedName("video")
    val video: VideoRemote,

    @SerializedName("instl")
    val instl: Int
) {
    fun toJson() : JSONObject  = JSONObject().apply {
        put("id", id)
        put("video", video.toJson())
        put("instl", instl)
    }
}