package com.mobileadvsdk.presentation

import android.content.Context
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.AdvertiseType

internal interface AdvProvider {

    fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener)

    fun initialize(
        context: Context,
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    )

    fun showAvd(id: String, iAdShowListener: IAdShowListener)
}