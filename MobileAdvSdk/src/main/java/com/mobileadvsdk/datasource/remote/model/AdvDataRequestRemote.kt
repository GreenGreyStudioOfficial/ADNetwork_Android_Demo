package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

internal class AdvDataRequestRemote(
    @SerializedName("id")
    val id: String,

    @SerializedName("test")
    val test: Int,

    @SerializedName("imp")
    val imp: List<ImpRemote>?,

    @SerializedName("app")
    val app: AppInfoRemote,

    @SerializedName("device")
    val device: DeviceRemote,

    @SerializedName("user")
    val user: UserRemote
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("test", test)
        imp?.let { list ->
            put("imp", JSONArray().apply {
                list.forEach { put(it.toJson()) }
            })
        }
        put("app", app.toJson() )
        put("device", device.toJson() )
        put("user", user.toJson() )
    }
}