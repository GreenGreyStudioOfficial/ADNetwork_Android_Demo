package com.mobileadvsdk

import android.app.Application
import androidx.annotation.Keep
import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.datasource.domain.model.AdvReqType
import com.mobileadvsdk.presentation.AdvProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Keep
object AdvSDK {

    internal var provider: AdvProviderImpl? = null
    internal lateinit var context: Application
    internal val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Keep
    fun initialize(
        context: Application,
        gameId: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        if (provider == null) {
            if (gameId.isEmpty()) {
                listener.onInitializationError(InitializationErrorType.GAME_ID_IS_NULL_OR_EMPTY, "")
                return
            }
            this.context = context
            provider = AdvProviderImpl(gameId = gameId, isTestMode = isTestMode, scope = scope)
            listener.onInitializationComplete()
        } else {
            listener.onInitializationError(InitializationErrorType.SDK_ALREADY_INITIALIZED, "")
        }
    }

    @Keep
    fun load(advertiseType: AdvertiseType, advReqType: AdvReqType = AdvReqType.DEFAULT, listener: IAdLoadListener) =
        provider?.loadAvd(advertiseType, advReqType, listener) ?: listener.onLoadError(LoadErrorType.NOT_INITIALIZED_ERROR)

    @Keep
    fun show(id: String, iAdShowListener: IAdShowListener) =
        provider?.showAvd(id, iAdShowListener) ?: iAdShowListener.onShowError(id, ShowErrorType.NOT_INITIALIZED_ERROR)
}