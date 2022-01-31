package com.mobileadvsdk.datasource.domain.model

data class AdvData(
    val id: String? = null,
    var advertiseType: AdvertiseType? = null,
    val bidid: String? = null,
    val seatbid: List<Seatbid> = mutableListOf()
)
