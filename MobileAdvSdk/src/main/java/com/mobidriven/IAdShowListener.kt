package com.mobidriven

import androidx.annotation.Keep
import com.mobidriven.datasource.domain.model.ShowCompletionState
import com.mobidriven.datasource.domain.model.ShowErrorType

@Keep
interface IAdShowListener {

    @Keep
    fun onShowChangeState(id: String, showCompletionState: ShowCompletionState)

    @Keep
    fun onShowError(id: String, error: ShowErrorType, errorMessage: String = "")
}