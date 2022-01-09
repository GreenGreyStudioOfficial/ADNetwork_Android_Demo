package com.mobileadvsdk.datasource.domain.model

data class Device(
    val ua: String,
    val geo: Geo,
    val ip: String,
    val deviceType: Int,
    val make: String,
    val model: String,
    val os: String,
    val osv: String,
    val w: Int,
    val h: Int,
    val connectionType: String,
    val ifa: String
)
