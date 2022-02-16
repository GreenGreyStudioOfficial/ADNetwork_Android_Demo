package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class DeviceRemote(

    @SerializedName("geo")
    val geo: GeoRemote?,

    @SerializedName("ip")
    val ip: String?,

    @SerializedName("devicetype")
    val deviceType: Int?,

    @SerializedName("make")
    val make: String?,

    @SerializedName("model")
    val model: String?,

    @SerializedName("os")
    val os: String?,

    @SerializedName("osv")
    val osv: String?,

    @SerializedName("w")
    val w: Int?,

    @SerializedName("h")
    val h: Int?,

    @SerializedName("connectiontype")
    val connectionType: Int?,

    @SerializedName("ifa")
    val ifa: String?
)
