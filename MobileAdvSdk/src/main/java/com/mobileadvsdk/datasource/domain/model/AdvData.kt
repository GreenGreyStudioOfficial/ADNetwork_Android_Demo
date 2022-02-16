package com.mobileadvsdk.datasource.domain.model

internal data class AdvData(
    val id: String? = null,
    var advertiseType: AdvertiseType?,
    val bidid: String?,
    val seatbid: List<Seatbid>
)
