package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class SeatbidRemote(
    @SerializedName("bid")
    val bid: List<BidRemote>?
)
