package com.mobidriven.datasource.domain.model

import org.json.JSONObject

internal data class ExtAdv(
    val cache_max: Long?,
    val cache_timeout: Long?,
    val req_timeout: Long?,
    val imp_timeout: Long?,
    val files: List<String>? ,
) {
    fun toJson(): JSONObject =JSONObject().apply {
        cache_max?.let { put("cache_max",it) }
        cache_timeout?.let { put("cache_timeout",it) }
        req_timeout?.let { put("req_timeout",it) }
        imp_timeout?.let { put("imp_timeout",it) }
        files?.let { put("files", files) }
    }
}
