package com.mobileadvsdk.presentation

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobileadvsdk.AdvApplication
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.di.KodeinHolder
import com.mobileadvsdk.di.mainModule
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.IOException

class AdvViewModel(adServerHost: String) : ViewModel(), AdvProvider, KodeinAware {

    val advDataLive: MutableLiveData<AdvData> = MutableLiveData()
    private val dataRepository: DataRepository by instance()
    private val scheduler: Scheduler by instance("uiScheduler")
    private val initDataLive: MutableLiveData<InitData> by lazy { MutableLiveData() }
    private val disposables: CompositeDisposable = CompositeDisposable()
    private var deviceInfo = DeviceInfo(
            "1",
            1,
            listOf(Imp("1", Video(listOf("video/mp4"), 1555, 692, Ext(0)), 1)),
            AppInfo("secret", "GGAD_NETWORK_SDK", "com.DefaultCompany.GGAD_NETWORK_SD"),
            Device(
                    geo = Geo(0.0, 0.0),
                    model = "OMEN Laptop 15-ek1xxx (HP)",
                    os = "Windows 10  (10.0.19042) 64bit",
                    w = 1555,
                    h = 692,
                    connectionType = 2,
                    ifa = "0947b09a6e1342c05c728adb8cdf88d3a3bb31f4"
            ),
            User("0aa5c2f9-cf04-42f5-b6dc-d71a5d05e258")
    )

    override val kodein: Kodein = Kodein { import(mainModule(adServerHost)) }.apply { KodeinHolder.kodein = this }

    override fun initialize(gameId: String, adServerHost: String, isTestMode: Boolean, listener: IAdInitializationListener) {
        initDataLive.postValue(InitData(gameId, adServerHost, isTestMode))
    }

    override fun loadAvd(listener: IAdLoadListener) {
        disposables += dataRepository.loadStartData(deviceInfo.copy(id = initDataLive.value?.gameId ?: ""))
                .observeOn(scheduler)
                .subscribeBy(
                        onSuccess = {
                            it?.let {
                                advDataLive.value = it
                                listener.onLoadComplete(it.seatbid[0].bid[0].id ?: "")
                            }
                        },
                        onError = {
                            when (it) {
                                is IOException -> {
                                    listener.onLoadError(
                                            LoadErrorType.CONNECTION_ERROR,
                                            LoadErrorType.CONNECTION_ERROR.desc
                                    )
                                }

                            }
                        }
                )
    }

    lateinit var iAdShowListener: IAdShowListener

    override fun showAvd(id: String, iAdShowListener: IAdShowListener) {
        advDataLive.value?.let {
            this.iAdShowListener = iAdShowListener
            AdvApplication.instance.startActivity(Intent(AdvApplication.instance, AdvActivity::class.java).addFlags(FLAG_ACTIVITY_NEW_TASK))
        } ?: iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
    }

    fun getUrl(url: String) {
        disposables += dataRepository.getNurl(url)
                .observeOn(scheduler)
                .subscribeBy(
                        onComplete = {

                        },
                        onError = {})
    }

    override fun onCleared() {
        disposables.clear()
    }
}