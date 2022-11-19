package com.mobidriven.datasource.remote.model

import androidx.annotation.Keep

@Keep
internal data class BidRemote(
    val id: String,
    val impid: String?,
    val nurl: String?,
    val lurl: String?,
    val adm: String,
    val cid: String?,
    val crid: String?,
    val api: Int?,
    val extAdv: ExtAdvRemote?,
)
