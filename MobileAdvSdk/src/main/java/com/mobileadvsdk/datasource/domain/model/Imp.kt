package com.mobileadvsdk.datasource.domain.model

internal data class Imp(
    val id: String,
    val video: Video? = null,
    val banner: Banner? = null,
    val instl: Int
)