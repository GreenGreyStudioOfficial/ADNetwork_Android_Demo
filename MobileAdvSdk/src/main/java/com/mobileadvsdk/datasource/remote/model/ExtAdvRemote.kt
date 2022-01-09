package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class ExtAdvRemote(

    @SerializedName("cache_max")
    val cache_max: Long,

    @SerializedName("cache_timeout")
    val cache_timeout: Long,

    @SerializedName("req_timeout")
    val req_timeout: Long,

    @SerializedName("imp_timeout")
    val imp_timeout: Long,
)
