package com.mobidriven.datasource.domain.model

import com.mobidriven.BuildConfig

internal data class Imp(
    val id: String,
    val video: Video? = null,
    val banner: Banner? = null,
    val instl: Int,
    val displaymanager: String = "MobidrivenSDK",
    val displaymanagerver: String = BuildConfig.PUBLISH_VERSION
)