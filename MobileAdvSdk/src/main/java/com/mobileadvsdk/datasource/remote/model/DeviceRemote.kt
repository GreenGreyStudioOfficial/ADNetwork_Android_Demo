package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class DeviceRemote(

    @SerializedName("geo")
    val geo: GeoRemote? = null,

    @SerializedName("ip")
    val ip: String? = null,

    @SerializedName("devicetype")
    val deviceType: Int? = null,

    @SerializedName("make")
    val make: String? = null,

    @SerializedName("model")
    val model: String? = null,

    @SerializedName("os")
    val os: String? = null,

    @SerializedName("osv")
    val osv: String? = null,

    @SerializedName("w")
    val w: Int? = null,

    @SerializedName("h")
    val h: Int? = null,

    @SerializedName("connectiontype")
    val connectionType: Int? = null,

    @SerializedName("ifa")
    val ifa: String? = null
)
