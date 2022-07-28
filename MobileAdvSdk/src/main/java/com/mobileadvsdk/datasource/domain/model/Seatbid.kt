package com.mobileadvsdk.datasource.domain.model

import org.json.JSONArray
import org.json.JSONObject

internal data class Seatbid(val bid: List<Bid>) {
    fun toJson()  = JSONObject().apply {
        put("bid", JSONArray().apply {
            bid.forEach { put(it.toJson()) }
        })
    }
}
