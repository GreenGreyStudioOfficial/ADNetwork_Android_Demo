package com.mobidriven

import androidx.annotation.Keep
import com.mobidriven.datasource.domain.model.ShowCompletionState
import com.mobidriven.datasource.domain.model.ShowErrorType

@Keep
interface IAdShowListener {

    @Keep
    fun onShowChangeState(id: String? = null, showCompletionState: ShowCompletionState)

    @Keep
    fun onShowError(error: ShowErrorType, errorMessage: String = "", id: String? = null)
}

@Keep
interface IAdShowBannerListener {

    @Keep
    fun onBannerShow(id: String? = null)

    @Keep
    fun onBannerShowError(error: ShowErrorType, errorMessage: String = "", id: String? = null)

    @Keep
    fun onBannerHide(id: String? = null)

    @Keep
    fun onBannerHideError(error: ShowErrorType, errorMessage: String = "", id: String? = null)
}