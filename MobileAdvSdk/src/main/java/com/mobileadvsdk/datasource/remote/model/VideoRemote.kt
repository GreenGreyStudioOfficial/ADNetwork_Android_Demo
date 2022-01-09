package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class VideoRemote(

    @SerializedName("mimes")
    val mimes: List<String> = mutableListOf(),

    @SerializedName("w")
    val w: Int,

    @SerializedName("h")
    val h: Int,

    @SerializedName("ext")
    val ext: ExtRemote
)
