package com.mobileadvsdk.datasource.domain.model

data class DeviceInfo(
    val id: String,
    val test: Int,
    val imp: List<Imp> = mutableListOf(),
    val app: AppInfo,
    val device: Device,
    val user: User
)