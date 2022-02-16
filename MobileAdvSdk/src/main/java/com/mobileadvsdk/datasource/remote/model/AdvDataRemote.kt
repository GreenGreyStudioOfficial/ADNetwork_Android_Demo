package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class AdvDataRemote(
    @SerializedName("id")
    val id: String?,

    @SerializedName("bidid")
    val bidid: String?,

    @SerializedName("seatbid")
    val seatbid: List<SeatbidRemote>?
)