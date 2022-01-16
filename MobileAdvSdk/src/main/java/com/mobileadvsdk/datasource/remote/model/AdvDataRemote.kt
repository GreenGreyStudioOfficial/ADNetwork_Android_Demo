package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class AdvDataRemote(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("bidid")
    val bidid: String? = null,

    @SerializedName("seatbid")
    val seatbid: List<SeatbidRemote> = mutableListOf()
)