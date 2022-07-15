package com.mobileadvsdk.datasource.domain.model

internal data class DeviceInfo(
    val id: String,
    val test: Int,
    val imp: List<Imp>,
    val app: AppInfo,
    val device: Device,
    val user: User
)