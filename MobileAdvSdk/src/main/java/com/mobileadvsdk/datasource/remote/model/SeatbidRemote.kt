package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class SeatbidRemote(
    @SerializedName("bid")
    val bid: List<BidRemote> = mutableListOf()
)
