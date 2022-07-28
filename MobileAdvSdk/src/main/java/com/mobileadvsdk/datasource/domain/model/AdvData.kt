package com.mobileadvsdk.datasource.domain.model

import com.mobileadvsdk.toJson
import org.json.JSONArray
import org.json.JSONObject

internal data class AdvData(
    val id: String,
    val advertiseType: AdvertiseType,
    val bidid: String?,
    val seatbid: List<Seatbid>
) {
    fun toJson() =  JSONObject().apply {
        put("id", id)
        put("advertiseType", advertiseType.toJson())
        bidid?.let {  put("bidid", it)  }
        put("seatbid", JSONArray().apply {
            seatbid.forEach { put(it.toJson()) }
        })
    }
}
