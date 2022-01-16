package com.mobileadvsdk

import android.content.Context
import com.mobileadvsdk.di.mainModule
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

class AdvManager {

    private val kodein = Kodein {
        import(mainModule())
    }

    private val provider: AdvViewModel by kodein.instance()

    fun loadData(listener: LoadDataListener) = provider.loadAvd(listener)

    fun showAdv(context: Context) = provider.showAvd(context)
}