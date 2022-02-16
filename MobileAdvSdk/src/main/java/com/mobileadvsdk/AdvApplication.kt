package com.mobileadvsdk

import android.app.Application


internal class AdvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AdvApplication
            private set
    }
}