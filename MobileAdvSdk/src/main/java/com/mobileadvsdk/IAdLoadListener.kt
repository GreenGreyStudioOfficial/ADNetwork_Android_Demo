package com.mobileadvsdk

import androidx.annotation.Keep
import com.mobileadvsdk.datasource.domain.model.LoadErrorType

@Keep
interface IAdLoadListener {

    @Keep
    fun onLoadComplete(id: String)

    @Keep
    fun onLoadError(error: LoadErrorType, errorMessage: String = "", id: String = "")
}