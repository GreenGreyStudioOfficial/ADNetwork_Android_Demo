package com.mobidriven.datasource.domain.model

internal data class Geo(
    val lat: Double? = 0.0,
    val lon: Double? = 0.0,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null
)
