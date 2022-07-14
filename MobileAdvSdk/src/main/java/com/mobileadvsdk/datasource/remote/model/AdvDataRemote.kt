package com.mobileadvsdk.datasource.remote.model


internal data class AdvDataRemote(
    val id: String?,
    val bidid: String?,
    val seatbid: List<SeatbidRemote>?
)