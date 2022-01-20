package com.mobileadvsdk.presentation

import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener

interface AdvProvider {

    fun loadAvd(listener: IAdLoadListener)


    fun getLoadedAds()

    fun initialize(
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    )

    fun showAvd(id: String, iAdShowListener: IAdShowListener)
}