package com.mobidriven.datasource.domain.model

import androidx.annotation.Keep
import com.mobidriven.datasource.remote.model.DeviceRemote
import com.mobidriven.datasource.remote.model.UserRemote
import com.mobidriven.toJson
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

internal data class AdvInitData(
    val device: Device,
    val user: User
)