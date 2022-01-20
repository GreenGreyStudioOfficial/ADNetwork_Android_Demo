package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.LoadErrorType

interface IAdLoadListener {

    fun onLoadComplete(id: String)

    fun onLoadError(error: LoadErrorType, id: String, errorMessage: String)
}