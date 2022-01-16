package com.mobileadvsdk

import android.content.Context
import org.kodein.di.generic.instance
import org.kodein.di.subKodein

object AdvManager {

    private val kodein = subKodein(KodeinHolder.kodein) {}

    private val provider: AdvViewModel by kodein.instance()

    fun loadData(listener: LoadDataListener) = provider.loadAvd(listener)

    fun showAdv(context: Context) = provider.showAvd(context)
}