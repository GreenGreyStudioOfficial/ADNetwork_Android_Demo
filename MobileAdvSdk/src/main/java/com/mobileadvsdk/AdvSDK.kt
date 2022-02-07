package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType
import com.mobileadvsdk.datasource.domain.model.LoadErrorType
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.presentation.AdvViewModel

object AdvSDK {

    var provider: AdvViewModel? = null

    fun initialize(
            gameId: String,
            adServerHost: String,
            isTestMode: Boolean,
            listener: IAdInitializationListener
    ) {
        if (provider == null) {
            if (gameId.isEmpty()){
                listener.onInitializationError(InitializationErrorType.GAME_ID_IS_NULL_OR_EMPTY, "")
                return
            }
            if (adServerHost.isEmpty()){
                listener.onInitializationError(InitializationErrorType.AD_SERVER_HOST_IS_NULL_OR_EMPTY, "")
                return
            }
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


    fun load(advertiseType: AdvertiseType, listener: IAdLoadListener) = provider?.let {
        provider?.loadAvd(advertiseType, listener)
    } ?: listener.onLoadError(LoadErrorType.NOT_INITIALIZED_ERROR)

    fun show(id: String, iAdShowListener: IAdShowListener) = provider?.let {
        provider?.showAvd(id, iAdShowListener)
    } ?: iAdShowListener.onShowError("", ShowErrorType.NOT_INITIALIZED_ERROR)
}