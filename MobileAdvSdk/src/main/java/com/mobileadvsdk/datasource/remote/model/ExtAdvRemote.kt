package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class ExtAdvRemote(

    @SerializedName("cache_max")
    val cache_max: Long? = null,

    @SerializedName("cache_timeout")
    val cache_timeout: Long? = null,

    @SerializedName("req_timeout")
    val req_timeout: Long? = null,

    @SerializedName("imp_timeout")
    val imp_timeout: Long? = null,
)
