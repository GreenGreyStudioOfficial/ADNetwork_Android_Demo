package com.mobileadvsdk.datasource.domain.model

data class DeviceInfo(
    var id: String,
    var test: Int,
    val imp: List<Imp> = mutableListOf(),
    var app: AppInfo,
    var device: Device,
    var user: User
)