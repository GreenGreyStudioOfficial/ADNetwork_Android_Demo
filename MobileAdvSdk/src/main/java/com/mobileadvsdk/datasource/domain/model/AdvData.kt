package com.mobileadvsdk.datasource.domain.model

data class AdvData(
    val id: String?,
    val bidid: String?,
    val seatbid: List<Seatbid> = mutableListOf()
)
