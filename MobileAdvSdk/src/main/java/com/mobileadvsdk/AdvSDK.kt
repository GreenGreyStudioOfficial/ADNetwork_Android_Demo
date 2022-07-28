package com.mobileadvsdk

import android.app.Application
import android.content.Context
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType
import com.mobileadvsdk.datasource.domain.model.LoadErrorType
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.presentation.AdvProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AdvSDK {

    internal var provider: AdvProviderImpl? = null
    internal lateinit var context: Application
    internal val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(
        context: Application,
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        if (provider == null) {
            if (gameId.isEmpty()) {
                listener.onInitializationError(InitializationErrorType.GAME_ID_IS_NULL_OR_EMPTY, "")
                return
            }
            if (adServerHost.isEmpty()) {
                listener.onInitializationError(InitializationErrorType.AD_SERVER_HOST_IS_NULL_OR_EMPTY, "")
                return
            }
            this.context = context
            provider = AdvProviderImpl(gameId = gameId, isTestMode = isTestMode, scope = scope)
            listener.onInitializationComplete()
        } else {
            listener.onInitializationError(InitializationErrorType.SDK_ALREADY_INITIALIZED, "")
        }
    }

    fun load(advertiseType: AdvertiseType, listener: IAdLoadListener) =
        provider?.loadAvd(advertiseType, listener) ?: listener.onLoadError(LoadErrorType.NOT_INITIALIZED_ERROR)

    fun show(id: String, iAdShowListener: IAdShowListener) =
        provider?.showAvd(id, iAdShowListener) ?: iAdShowListener.onShowError(id, ShowErrorType.NOT_INITIALIZED_ERROR)
}