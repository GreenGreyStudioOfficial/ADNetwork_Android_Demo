package com.mobileadvsdk

import android.content.Context
import com.mobileadvsdk.di.mainModule
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

object AdvManager {
    private val kodein = Kodein { import(mainModule()) }.apply { KodeinHolder.kodein = this }

    private val provider: AdvViewModel by kodein.instance()

    fun loadData(listener: LoadDataListener) = provider.loadAvd(listener)

    fun showAdv(context: Context) = provider.showAvd(context)
}