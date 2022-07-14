package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

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
) {
    fun toJson(): JSONObject =JSONObject().apply {
        lat?.let { put("lat",it) }
        lon?.let { put("lon",it) }
        country?.let { put("country",it) }
        region?.let { put("region",it) }
        city?.let { put("city",it) }
    }
}
