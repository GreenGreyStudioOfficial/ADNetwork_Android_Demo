package com.mobileadvsdk.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject
@Keep
internal data class DeviceRemote(
    val geo: GeoRemote?,
    val ip: String?,
    val deviceType: Int?,
    val make: String?,
    val model: String?,
    val os: String?,
    val osv: String?,
    val w: Int?,
    val h: Int?,
    val connectionType: Int?,
    val ifa: String?
) {
    fun toJson(): JSONObject = JSONObject().apply {
        geo?.let { put("geo", it.toJson()) }
        ip?.let { put("ip", it) }
        deviceType?.let { put("devicetype", it) }
        make?.let { put("make", it) }
        model?.let { put("model", it) }
        os?.let { put("os", it) }
        osv?.let { put("osv", it) }
        w?.let { put("w", it) }
        h?.let { put("h", it) }
        connectionType?.let { put("connectiontype", it) }
        ifa?.let { put("ifa", it) }
    }
}
