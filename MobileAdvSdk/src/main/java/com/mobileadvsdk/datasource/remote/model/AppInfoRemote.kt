package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class AppInfoRemote(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("bundle")
    val bundle: String
)
