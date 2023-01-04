package com.mobidriven.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject
@Keep
internal data class GeoRemote(
    val lat: Double?,
    val lon: Double?,
    val country: String?,
    val region: String?,
    val city: String?
) {
    fun toJson(): JSONObject =JSONObject().apply {
        lat?.let { put("lat",it) }
        lon?.let { put("lon",it) }
        country?.let { put("country",it) }
        region?.let { put("region",it) }
        city?.let { put("city",it) }
    }
}
