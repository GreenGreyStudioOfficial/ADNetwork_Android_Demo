package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class GeoRemote(

    @SerializedName("lat")
    val lat: Double?,

    @SerializedName("lon")
    val lon: Double?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("region")
    val region: String?,

    @SerializedName("city")
    val city: String?,
)
