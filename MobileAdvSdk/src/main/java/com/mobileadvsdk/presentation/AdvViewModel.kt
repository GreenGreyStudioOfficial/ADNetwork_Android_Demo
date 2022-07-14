package com.mobileadvsdk.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.data.DataRepositoryImpl
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.presentation.player.VASTParser
import com.mobileadvsdk.presentation.player.model.VASTModel
import com.mobileadvsdk.presentation.player.processor.CacheFileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.IOException

internal class AdvViewModel(context: Application, adServerHost: String) : AndroidViewModel(context), AdvProvider {

    val advDataLive: MutableStateFlow<AdvData?> = MutableStateFlow(null)
    var vastModel: VASTModel? = null

    private val dataRepository: DataRepository = DataRepositoryImpl()

    private val initDataLive: MutableStateFlow<InitData?> = MutableStateFlow(null)
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
        AppInfo("secret", "GGAD_NETWORK_SDK", context.packageName),
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

    override fun initialize(
        context: Context,
        gameId: String,
        adServerHost: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        initDataLive.value = InitData(gameId, adServerHost, isTestMode)
    }

    override fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        makeRequest(advertiseType, listener)
    }

    lateinit var iAdShowListener: IAdShowListener

    private fun context(): Context = getApplication<Application>().applicationContext

    private fun parseAdvData(lurl: String?, vast: String) {
        VASTParser.setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {
                iAdShowListener.onShowError("", ShowErrorType.VIDEO_DATA_NOT_FOUND)
                getUrl(lurl ?: "")
            }

            override fun onVASTCacheError(error: Int) {
                iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND)
            }

            override fun onVASTParserFinished(model: VASTModel?) {
                vastModel = model
                context().startActivity(
                    Intent(
                        context(),
                        AdvActivity::class.java
                    ).addFlags(FLAG_ACTIVITY_NEW_TASK)
                )
            }
        })
        viewModelScope.launch {
            VASTParser.parseVast(context(), vast)
        }
    }

    override fun showAvd(id: String, iAdShowListener: IAdShowListener) {
        advDataLive.value?.let {
            this.iAdShowListener = iAdShowListener
            parseAdvData(it.seatbid.first().bid.first().lurl, it.seatbid.firstOrNull()?.bid?.firstOrNull()?.adm ?: "")
        } ?: run {
            CacheFileManager.clearCache(context())
            iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
        }
    }

    fun getUrl(url: String) {
        viewModelScope.launch {
            dataRepository.getUrl(url)
                .catch { Log.e("AdvViewModel", "Error: ${it.localizedMessage}") }
                .onCompletion { Log.v("AdvViewModel", "complete") }
                .collect()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val manager = context().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(
                context(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
        viewModelScope.launch {
            dataRepository.loadStartData(deviceInfo)
                .catch {
                    when (it) {
                        is IOException -> {
                            listener.onLoadError(
                                LoadErrorType.CONNECTION_ERROR,
                                LoadErrorType.CONNECTION_ERROR.desc
                            )
                        }

                    }
                }
                .collect {
                    advDataLive.value = it.apply { this.advertiseType = advertiseType }
                    listener.onLoadComplete(it.seatbid.firstOrNull()?.bid?.firstOrNull()?.id ?: "")
                }
        }

    }
}

