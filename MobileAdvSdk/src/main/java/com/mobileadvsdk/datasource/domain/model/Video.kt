package com.mobileadvsdk.datasource.domain.model

internal data class Video(
    val mimes: List<String> = mutableListOf(),
    val w: Int,
    val h: Int,
    val ext: Ext
)
