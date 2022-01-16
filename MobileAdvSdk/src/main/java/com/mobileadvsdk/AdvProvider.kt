package com.mobileadvsdk

import android.content.Context

interface AdvProvider {

    fun loadAvd(listener: LoadDataListener)

    fun showAvd(context: Context)
}