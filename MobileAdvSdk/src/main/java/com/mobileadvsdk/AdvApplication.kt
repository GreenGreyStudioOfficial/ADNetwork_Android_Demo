package com.mobileadvsdk

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class AdvApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        instance = this
    }

    companion object {
        lateinit var instance: AdvApplication
            private set
    }
}