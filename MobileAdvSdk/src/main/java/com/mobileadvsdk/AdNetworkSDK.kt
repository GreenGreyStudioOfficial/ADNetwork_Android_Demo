package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType
import com.mobileadvsdk.presentation.AdvProvider
import com.mobileadvsdk.presentation.AdvViewModel

object AdNetworkSDK {

    private var provider: AdvProvider? = null

    fun initialize(
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        if (provider == null) {
            provider = AdvViewModel(adServerHost)
            init(gameId, adServerHost, isTestMode, listener)
            listener.onInitializationComplete()
        } else {
            listener.onInitializationError(InitializationErrorType.SDK_ALREADY_INITIALIZED, "")
        }
    }


    private fun init(
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) = provider?.initialize(gameId, adServerHost, isTestMode, listener)


    fun load(advertiseType: AdvertiseType, listener: IAdLoadListener) = provider?.loadAvd(listener)

    fun lazyLoad(advertiseType: AdvertiseType, listener: IAdLoadListener) =
        provider?.loadAvd(listener)

    fun show(id: String, iAdShowListener: IAdShowListener) = provider?.showAvd(id, iAdShowListener)


}