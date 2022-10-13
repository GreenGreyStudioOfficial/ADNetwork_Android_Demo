package com.mobileadvsdk

import androidx.annotation.Keep

@Keep
internal interface LoadDataListener {
    @Keep
    fun dataLoadSuccess()
    @Keep
    fun dataLoadFailure(){}
}