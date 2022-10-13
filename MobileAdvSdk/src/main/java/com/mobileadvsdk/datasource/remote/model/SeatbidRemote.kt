package com.mobileadvsdk.datasource.remote.model

import androidx.annotation.Keep

@Keep
internal data class SeatbidRemote(
    val bid: List<BidRemote>?
)
