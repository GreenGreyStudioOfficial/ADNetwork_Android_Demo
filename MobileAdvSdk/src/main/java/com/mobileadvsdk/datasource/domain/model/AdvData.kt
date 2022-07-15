package com.mobileadvsdk.datasource.domain.model

internal data class AdvData(
    val id: String? = null,
    val advertiseType: AdvertiseType?,
    val bidid: String?,
    val seatbid: List<Seatbid>
)
