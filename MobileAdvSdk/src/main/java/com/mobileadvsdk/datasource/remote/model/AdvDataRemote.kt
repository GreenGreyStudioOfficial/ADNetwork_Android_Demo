package com.mobileadvsdk.datasource.remote.model

import androidx.annotation.Keep


@Keep
internal data class AdvDataRemote(
    val id: String,
    val bidid: String?,
    val seatbid: List<SeatbidRemote>?
)