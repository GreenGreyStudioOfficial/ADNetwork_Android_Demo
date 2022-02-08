package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class ExtRemote(
    @SerializedName("rewarded")
    val rewarded: Long,
)
