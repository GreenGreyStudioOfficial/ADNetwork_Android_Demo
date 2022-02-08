package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal class AdvDataRequestRemote(
    @SerializedName("id")
    val id: String,

    @SerializedName("test")
    val test: Int,

    @SerializedName("imp")
    val imp: List<ImpRemote> = mutableListOf(),

    @SerializedName("app")
    val app: AppInfoRemote,

    @SerializedName("device")
    val device: DeviceRemote,

    @SerializedName("user")
    val user: UserRemote
)