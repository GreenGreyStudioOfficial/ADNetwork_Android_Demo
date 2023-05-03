package com.mobidriven.datasource.domain.model

import com.mobidriven.getDoubleOrNull
import com.mobidriven.getIntOrNull
import com.mobidriven.getJsonObjectOrNull
import com.mobidriven.getStringOrNull
import org.json.JSONObject

internal data class Device(
    var geo: Geo?,
    val ip: String? = null,
    val deviceType: Int? = null,
    val make: String? = null,
    val model: String,
    val os: String,
    val osv: String? = null,
    val w: Int,
    val h: Int,
    val connectionType: Int,
    val ifa: String?
)

internal fun String.fromJson(): Device {
    return JSONObject(this).run {
        val geo: Geo? = getJsonObjectOrNull("geo")?.run {
            val lat = getDoubleOrNull("lat")
            val lon = getDoubleOrNull("lon")
            val country = getStringOrNull("country")
            val region = getStringOrNull("region")
            val city = getStringOrNull("city")
            Geo(lat, lon, country, region, city)
        }
        val ip = getStringOrNull("ip")
        val deviceType = getIntOrNull("devicetype")
        val make = getStringOrNull("make")
        val model = getString("model")
        val os = getString("os")
        val osv = getStringOrNull("osv")
        val w = getInt("w")
        val h = getInt("h")
        val connectionType = getInt("connectiontype")
        val ifa = getStringOrNull("ifa")
        Device(geo, ip, deviceType, make, model, os, osv, w, h, connectionType, ifa)
    }
}
