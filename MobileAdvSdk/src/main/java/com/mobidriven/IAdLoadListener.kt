package com.mobidriven

import androidx.annotation.Keep
import com.mobidriven.datasource.domain.model.LoadErrorType

@Keep
interface IAdLoadListener {

    @Keep
    fun onLoadComplete(id: String)

    @Keep
    fun onLoadError(error: LoadErrorType, errorMessage: String = "", id: String = "")
}