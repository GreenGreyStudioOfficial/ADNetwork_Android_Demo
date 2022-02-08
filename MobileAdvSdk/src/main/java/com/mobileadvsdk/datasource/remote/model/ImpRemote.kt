package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class ImpRemote(

    @SerializedName("id")
    val id: String,

    @SerializedName("video")
    val video: VideoRemote,

    @SerializedName("instl")
    val instl: Int
)