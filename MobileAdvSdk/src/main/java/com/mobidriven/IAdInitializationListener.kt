package com.mobidriven

import androidx.annotation.Keep
import com.mobidriven.datasource.domain.model.InitializationErrorType

@Keep
 interface IAdInitializationListener {

    @Keep
    fun onInitializationComplete()

    @Keep
    fun onInitializationError(error: InitializationErrorType, errorMessage: String)
}