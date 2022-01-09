package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class BidRemote(
    @SerializedName("id")
    val id: String,

    @SerializedName("impid")
    val impid: String,

    @SerializedName("nurl")
    val nurl: String,

    @SerializedName("lurl")
    val lurl: String,

    @SerializedName("adm")
    val adm: String,

    @SerializedName("cid")
    val cid: String,

    @SerializedName("crid")
    val crid: String,

    @SerializedName("ext")
    val extAdv: ExtAdvRemote,
)
