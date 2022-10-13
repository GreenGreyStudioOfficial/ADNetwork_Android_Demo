package com.mobileadvsdk

import androidx.annotation.Keep
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType

@Keep
 interface IAdInitializationListener {

    @Keep
    fun onInitializationComplete()

    @Keep
    fun onInitializationError(error: InitializationErrorType, errorMessage: String)
}