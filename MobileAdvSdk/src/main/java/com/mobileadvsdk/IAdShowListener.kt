package com.mobileadvsdk

import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType

interface IAdShowListener {

    fun onShowChangeState(id: String, showCompletionState: ShowCompletionState)

    fun  onShowError(id: String, error: ShowErrorType, errorMessage: String)
}