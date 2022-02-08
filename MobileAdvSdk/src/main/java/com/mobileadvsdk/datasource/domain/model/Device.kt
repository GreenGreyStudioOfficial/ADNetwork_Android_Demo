package com.mobileadvsdk.datasource.domain.model

internal data class Device(
    var geo: Geo,
    val ip: String? = null,
    val deviceType: Int? = null,
    val make: String? = null,
    val model: String,
    val os: String,
    val osv: String? = null,
    val w: Int,
    val h: Int,
    val connectionType: Int,
    val ifa: String
)
