package com.mobileadvsdk

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.mobileadvsdk.datasource.di.dataSourceModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware

class AdvApplication:MultiDexApplication(), KodeinAware {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }

    override val kodein: Kodein by Kodein.lazy {
        import(dataSourceModule)
    }
}