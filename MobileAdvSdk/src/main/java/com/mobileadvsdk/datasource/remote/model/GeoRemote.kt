package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class GeoRemote(

    @SerializedName("lat")
    val lat: Double?=null,

    @SerializedName("lon")
    val lon: Double?=null,

    @SerializedName("country")
    val country: String?=null,

    @SerializedName("region")
    val region: String?=null,

    @SerializedName("city")
    val city: String?=null,

)
