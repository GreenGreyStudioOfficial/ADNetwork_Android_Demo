package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

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
