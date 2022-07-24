package com.mobileadvsdk.datasource.domain.model

internal data class Video(
    val mimes: List<String> = listOf("video/mp4"),
    val w: Int,
    val h: Int,
    val ext: Ext
)

internal data class Banner(
    val mimes: List<String> = listOf("application/javascript"),
    val w: Int,
    val h: Int,
    val ext: Ext,
    val api: List<Int> = listOf(5)
)
