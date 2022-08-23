package com.mobileadvsdk.datasource.remote.model


internal data class ExtAdvRemote(
    val cache_max: Long?,
    val cache_timeout: Long?,
    val req_timeout: Long?,
    val imp_timeout: Long?,
    val files: List<String>?,
)
