package com.mobileadvsdk.datasource.domain.model

internal data class AdvData(
    val id: String? = null,
    var advertiseType: AdvertiseType? = null,
    val bidid: String? = null,
    val seatbid: List<Seatbid> = mutableListOf()
)
