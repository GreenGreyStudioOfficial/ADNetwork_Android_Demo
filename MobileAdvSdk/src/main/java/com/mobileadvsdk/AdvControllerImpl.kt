package com.mobileadvsdk

import androidx.lifecycle.ViewModel
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.*
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

class AdvControllerImpl(
    private val dataRepository: DataRepository,
    private val scheduler: Scheduler
) : ViewModel(), AdvController {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val deviceInfo = DeviceInfo(
        "1",
        1,
        listOf(Imp("1", Video(listOf("video/mp4"), 799, 268, Ext(0)), 1)),
        AppInfo("secret", "GGAD_NETWORK_SDK", "com.DefaultCompany.GGAD_NETWORK_SD"),
        Device(
            geo = Geo(0.0, 0.0),
            model = "OMEN Laptop 15-ek1xxx (HP)",
            os = "Windows 10  (10.0.19042) 64bit",
            w = 799,
            h = 268,
            connectionType = 2,
            ifa = "0947b09a6e1342c05c728adb8cdf88d3a3bb31f4"
        ),
        User("0aa5c2f9-cf04-42f5-b6dc-d71a5d05e258")
    )

    override fun loadAvd() {

        disposables += dataRepository.loadStartData(deviceInfo)
            .observeOn(scheduler)
            .subscribeBy(
                onSuccess = {},
                onError = {}
            )

    }

    override fun showAvd() {

    }

    override fun onCleared() {
        disposables.clear()
    }
}