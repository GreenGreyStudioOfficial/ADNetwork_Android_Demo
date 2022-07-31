package com.mobileadvsdk.datasource.domain.model

import org.json.JSONObject

internal data class Bid(
    val id: String,
    val impid: String? = null,
    val nurl: String? = null,
    val lurl: String? = null,
    val adm: String,
    val cid: String? = null,
    val crid: String? = null,
    val api: Int? = null,
    val extAdv: ExtAdv? = null,
) {
    fun toJson(): JSONObject  = JSONObject().apply{
        put("id", id)
        impid?.let { put("impid", it) }
        nurl?.let { put("nurl", it) }
        lurl?.let { put("lurl", it) }
        put("adm", adm)
        cid?.let { put("cid", it) }
        crid?.let { put("crid", it) }
        api?.let { put("api", it) }
        extAdv?.let { put("extAdv", it.toJson()) }
    }
}
