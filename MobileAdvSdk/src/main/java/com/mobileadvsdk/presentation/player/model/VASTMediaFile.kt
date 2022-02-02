package com.mobileadvsdk.presentation.player.model

import java.math.BigInteger

class VASTMediaFile {
    var value: String? = null
    var id: String? = null
    var delivery: String? = null
    var type: String? = null
    var bitrate: BigInteger? = null
    var width: BigInteger? = null
    var height: BigInteger? = null
    var isScalable: Boolean? = null
    var isMaintainAspectRatio: Boolean? = null
    var apiFramework: String? = null
    override fun toString(): String {
        return "MediaFile [value=$value, id=$id, delivery=$delivery, type=$type, bitrate=$bitrate, width=$width, height=$height, scalable=$isScalable, maintainAspectRatio=$isMaintainAspectRatio, apiFramework=$apiFramework]"
    }
}