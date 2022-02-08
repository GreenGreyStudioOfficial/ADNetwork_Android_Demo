package com.mobileadvsdk.datasource.remote.model

import com.google.gson.annotations.SerializedName

internal data class BidRemote(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("impid")
    val impid: String? = null,

    @SerializedName("nurl")
    val nurl: String? = null,

    @SerializedName("lurl")
    val lurl: String? = null,

    @SerializedName("adm")
    val adm: String? = null,

    @SerializedName("cid")
    val cid: String? = null,

    @SerializedName("crid")
    val crid: String? = null,

    @SerializedName("ext")
    val extAdv: ExtAdvRemote?= null,
)
