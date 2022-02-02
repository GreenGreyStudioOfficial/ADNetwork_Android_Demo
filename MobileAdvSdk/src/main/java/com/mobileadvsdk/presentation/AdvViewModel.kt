package com.mobileadvsdk.presentation

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
import net.pubnative.player.VASTParser
import net.pubnative.player.model.VASTModel
import net.pubnative.player.processor.CacheFileManager
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.IOException

class AdvViewModel(adServerHost: String) : ViewModel(), AdvProvider, KodeinAware {

    val advDataLive: MutableLiveData<AdvData> = MutableLiveData()
    var vastModel: VASTModel? = null
    private val dataRepository: DataRepository by instance()
    private val scheduler: Scheduler by instance("uiScheduler")
    private val initDataLive: MutableLiveData<InitData> by lazy { MutableLiveData() }
    private val disposables: CompositeDisposable = CompositeDisposable()
    private var deviceInfo = DeviceInfo(
        "1",
        1,
        listOf(
            Imp(
                "1",
                Video(
                    listOf("video/mp4"),
                    Resources.getSystem().displayMetrics.widthPixels,
                    Resources.getSystem().displayMetrics.heightPixels,
                    Ext(0)
                ),
                1
            )
        ),
        AppInfo("secret", "GGAD_NETWORK_SDK", "com.DefaultCompany.GGAD_NETWORK_SD"),
        Device(
            geo = Geo(null, null),
            model = "OMEN Laptop 15-ek1xxx (HP)",
            os = "Windows 10  (10.0.19042) 64bit",
            w = Resources.getSystem().displayMetrics.widthPixels,
            h = Resources.getSystem().displayMetrics.heightPixels,
            connectionType = 2,
            ifa = "0947b09a6e1342c05c728adb8cdf88d3a3bb31f4"
        ),
        User("0aa5c2f9-cf04-42f5-b6dc-d71a5d05e258")
    )

    override val kodein: Kodein =
        Kodein { import(mainModule(adServerHost)) }.apply { KodeinHolder.kodein = this }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initialize(
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        initDataLive.postValue(InitData(gameId, adServerHost, isTestMode))

        AdvApplication.instance.startActivity(
            Intent(
                AdvApplication.instance,
                PermissionsActivity::class.java
            ).addFlags(FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        makeRequest(advertiseType, listener)
    }

    lateinit var iAdShowListener: IAdShowListener

    private fun parseAdvData(lurl: String?, vast: String) =
        VASTParser(AdvApplication.instance).setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {
                Log.e("onVASTParserError", "error: $error")
                iAdShowListener.onShowError("", ShowErrorType.VIDEO_DATA_NOT_FOUND)
                getUrl(lurl ?: "")
            }

            override fun onVASTCacheError(error: Int) {
                iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND)
                Log.e("onVASTCacheError", "error: $error")
            }

            override fun onVASTParserFinished(model: VASTModel?) {
                vastModel = model
                AdvApplication.instance.startActivity(
                    Intent(
                        AdvApplication.instance,
                        AdvActivity::class.java
                    ).addFlags(FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }).execute(vast)

    override fun showAvd(id: String, iAdShowListener: IAdShowListener) {
        advDataLive.value?.let {
            this.iAdShowListener = iAdShowListener
            parseAdvData(it.seatbid[0].bid[0].lurl, it.seatbid[0].bid[0].adm ?: "")
        } ?: run {
            CacheFileManager.instance?.clearCache(AdvApplication.instance)
            iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
        }
    }

    fun getUrl(url: String) {
        disposables += dataRepository.getNurl(url)
            .observeOn(scheduler)
            .subscribeBy(
                onComplete = {},
                onError = {})
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun getLastLocation() {
        val manager =
            AdvApplication.instance.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                AdvApplication.instance,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            var utilLocation: Location? = null
            val providers = manager.getProviders(true)
            for (provider in providers) {
                provider?.let { it ->
                    manager.getLastKnownLocation(it)?.let {
                        utilLocation = it
                    }
                }
            }
            deviceInfo.apply { device.geo = Geo(utilLocation?.latitude, utilLocation?.longitude) }
        }
    }

    private fun makeRequest(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        getLastLocation()
        disposables += dataRepository.loadStartData(deviceInfo)
            .observeOn(scheduler)
            .subscribeBy(
                onSuccess = {
                    it?.let {
                        advDataLive.value = it.apply { this.advertiseType = advertiseType }
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
}