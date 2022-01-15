package com.mobileadvsdk

import com.mobileadvsdk.di.mainModule
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class AdvManager() {

    private val kodein = Kodein {
        import(mainModule())
    }

    private val controller: AdvController by kodein.instance()

    fun loadData() = controller.loadAvd()


    fun showAdv() {}


}