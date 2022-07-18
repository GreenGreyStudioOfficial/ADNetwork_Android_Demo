package com.mobileadvsdk.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.data.DataRepositoryImpl
import com.mobileadvsdk.datasource.data.Prefs
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.*
import com.mobileadvsdk.presentation.player.VASTParser
import com.mobileadvsdk.presentation.player.model.VASTModel
import com.mobileadvsdk.presentation.player.processor.CacheFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


internal class AdvProviderImpl(val gameId: String, isTestMode: Boolean = false, val scope: CoroutineScope) {

    private val _advDataFlow: MutableStateFlow<AdvData?> = MutableStateFlow(null)
    internal val advData: AdvData?
        get() = _advDataFlow.asStateFlow().value
    private val dataRepository: DataRepository = DataRepositoryImpl()
    private val _deviceInfoFlow: MutableStateFlow<DeviceInfo> = MutableStateFlow(defaultDeviceInfo(isTestMode, gameId))
    private val deviceInfo: DeviceInfo
        get() = _deviceInfoFlow.asStateFlow().value
    private val bid
        get() = advData?.seatbid?.firstOrNull()?.bid?.firstOrNull()
    var vastModel: VASTModel? = null

    lateinit var iAdShowListener: IAdShowListener

    fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        makeRequest(advertiseType, listener)
    }

    fun showAvd(id: String, adShowListener: IAdShowListener) {
        iAdShowListener = adShowListener
        advData?.let {
            parseAdvData(it.seatbid.first().bid.first().lurl, it.seatbid.firstOrNull()?.bid?.firstOrNull()?.adm ?: "")
        } ?: run {
            CacheFileManager.clearCache()
            iAdShowListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
        }
    }

    private fun makeRequest(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        getLastLocation()
        scope.launch {
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
                    _advDataFlow.value = it.copy(advertiseType = advertiseType)
                    listener.onLoadComplete(it.seatbid.firstOrNull()?.bid?.firstOrNull()?.id ?: "")
                }
        }
    }

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
                AdvSDK.context.startActivity(
                    Intent(
                        AdvSDK.context,
                        AdvActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        })
        scope.launch {
            VASTParser.parseVast(AdvSDK.context, vast)
        }
    }

    private fun getUrl(url: String) {
        scope.launch {
            dataRepository.getUrl(url)

        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val manager = AdvSDK.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                AdvSDK.context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(
                AdvSDK.context, Manifest.permission.ACCESS_FINE_LOCATION
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
            val device = deviceInfo.device
            _deviceInfoFlow.value =
                deviceInfo.copy(device = device.copy(geo = Geo(utilLocation?.latitude, utilLocation?.longitude)))
        }
    }

    fun playerLoadFinish() {
        getUrl(bid?.nurl ?: "")
    }

    fun showError(type: ShowErrorType, message: String = "") {
        iAdShowListener.onShowError(
            bid?.id ?: "",
            type,
            message
        )
    }

    fun playerPlaybackFinish() {
        _advDataFlow.value = null
    }
}

@SuppressLint("MissingPermission")
private fun getConnectionType(): Int {
    val context = AdvSDK.context
    val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = cm.activeNetwork
    val actNw = cm.getNetworkCapabilities(networkCapabilities) ?: return 0
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> 1
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 2
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            ) {
                when (tm.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN,
                    TelephonyManager.NETWORK_TYPE_GSM -> 4
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> 5
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> 6
                    TelephonyManager.NETWORK_TYPE_NR -> 6
                    else -> 3
                }
            } else 3
        }
        else -> 0
    }
}

@SuppressLint("HardwareIds")
private fun defaultDeviceInfo(isTestMode: Boolean, gameId: String) = DeviceInfo(
    id = UUID.randomUUID().toString(),
    test = if (isTestMode) 1 else 0,
    listOf(
        Imp(
            "1",
            Video(
                listOf("video/mp4"),
                Resources.getSystem().displayMetrics.widthPixels,
                Resources.getSystem().displayMetrics.heightPixels,
                Ext(0)
            ),
            instl = 1
        )
    ),
    AppInfo(
        gameId,
        AdvSDK.context.applicationInfo.loadLabel(AdvSDK.context.packageManager).toString(),
        AdvSDK.context.packageName
    ),
    Device(
        geo = Geo(),
        deviceType = 0,//TODO,
        make = Build.MANUFACTURER,
        model = Build.MODEL,
        os = "Android ${Build.VERSION.SDK_INT}",
        osv = "${Build.VERSION.SDK_INT}",
        w = Resources.getSystem().displayMetrics.widthPixels,
        h = Resources.getSystem().displayMetrics.heightPixels,
        ifa = Settings.Secure.getString(AdvSDK.context.contentResolver, Settings.Secure.ANDROID_ID),
        connectionType = getConnectionType()
    ),
    User(Prefs.userId)
)