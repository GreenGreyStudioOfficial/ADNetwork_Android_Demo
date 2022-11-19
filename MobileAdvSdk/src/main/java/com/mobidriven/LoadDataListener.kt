package com.mobidriven

import androidx.annotation.Keep

@Keep
internal interface LoadDataListener {
    @Keep
    fun dataLoadSuccess()
    @Keep
    fun dataLoadFailure(){}
}