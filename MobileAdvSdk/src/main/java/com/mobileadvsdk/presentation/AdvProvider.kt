package com.mobileadvsdk.presentation

import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.AdvertiseType

interface AdvProvider {

    fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener)

    fun initialize(
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    )

    fun showAvd(id: String, iAdShowListener: IAdShowListener)
}