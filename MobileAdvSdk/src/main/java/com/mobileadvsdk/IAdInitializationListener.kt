package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.InitializationErrorType

interface IAdInitializationListener {

    fun onInitializationComplete()

    fun onInitializationError(error: InitializationErrorType, errorMessage: String)
}