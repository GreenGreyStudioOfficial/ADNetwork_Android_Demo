package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

internal data class VideoRemote(

    @SerializedName("mimes")
    val mimes: List<String>?,

    @SerializedName("w")
    val w: Int,

    @SerializedName("h")
    val h: Int,

    @SerializedName("ext")
    val ext: ExtRemote
) {
    fun toJson(): JSONObject = JSONObject().apply {
        mimes?.let { list ->
            put("mimes", JSONArray().apply {
                list.forEach { put(it) }
            })
        }
        put("w",w)
        put("h",h)
        put("ext",ext.toJson())
    }
}
