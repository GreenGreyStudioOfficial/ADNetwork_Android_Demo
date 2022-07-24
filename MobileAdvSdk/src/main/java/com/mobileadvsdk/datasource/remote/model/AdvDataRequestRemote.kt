package com.mobileadvsdk.datasource.remote.model

import org.json.JSONArray
import org.json.JSONObject

internal class AdvDataRequestRemote(
    val id: String,
    val test: Int,
    val imp: List<ImpRemote>,
    val app: AppInfoRemote,
    val device: DeviceRemote,
    val user: UserRemote
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("test", test)
        put("imp", JSONArray().apply {
            imp.forEach { put(it.toJson()) }
        })
        put("app", app.toJson())
        put("device", device.toJson())
        put("user", user.toJson())
    }
}