package com.mobileadvsdk.datasource.remote.model


internal data class BidRemote(
    val id: String?,
    val impid: String?,
    val nurl: String?,
    val lurl: String?,
    val adm: String?,
    val cid: String?,
    val crid: String?,
    val extAdv: ExtAdvRemote?,
)
