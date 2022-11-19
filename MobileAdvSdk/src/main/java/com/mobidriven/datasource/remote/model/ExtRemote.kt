package com.mobidriven.datasource.remote.model

import androidx.annotation.Keep
import org.json.JSONObject
@Keep
internal data class ExtRemote(
    val rewarded: Long,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("rewarded", rewarded)
    }
}
