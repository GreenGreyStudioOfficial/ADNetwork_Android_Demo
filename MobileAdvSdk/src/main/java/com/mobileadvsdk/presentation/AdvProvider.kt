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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


internal class AdvProviderImpl(val gameId: String, val isTestMode: Boolean = false, val scope: CoroutineScope) {

    private val _advDataFlow: MutableStateFlow<AdvData?> = MutableStateFlow(null)

    private val dataRepository: DataRepository = DataRepositoryImpl()

    var vastModel: VASTModel? = null

    private val advData: AdvData?
        get() = _advDataFlow.asStateFlow().value ?: CacheFileManager.loadAdv(advId)

    private val bid
        get() = advData?.seatbid?.first()?.bid?.first()
    private var advId: String? = null

    internal val advType: AdvertiseType
        get() = if (advData?.advertiseType == AdvertiseType.REWARDED) AdvertiseType.REWARDED else AdvertiseType.INTERSTITIAL
    internal val adm: String?
        get() = bid?.adm

    lateinit var showListener: IAdShowListener
    lateinit var loadListener: IAdLoadListener

    fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        loadListener = listener
        makeRequest(advertiseType, listener = listener)
    }

    fun showAvd(id: String, adShowListener: IAdShowListener) {
        advId = id
        showListener = adShowListener
        advData?.let {
            val bid = it.seatbid.first().bid.first()
            if (listOf(5, 6).contains(bid.api) || bid.adm.startsWith("<!DOCTYPE html>")) {
                showMraid(bid.id)
            } else {
                parseAdvData(bid.lurl, bid.adm )
            }
        } ?: run {
            CacheFileManager.clearCache()
            showListener.onShowError(id, ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
        }
    }

    private fun showMraid(id: String?) {
        AdvSDK.context.startActivity(
            Intent(
                AdvSDK.context,
                WebviewActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun makeRequest(
        advertiseType: AdvertiseType,
        listener: IAdLoadListener
    ) {
        val advReqType: AdvReqType = AdvReqType.BANNER
        val deviceInfo = makeDeviceInfo(isTestMode, gameId, advReqType, advertiseType)

        scope.launch {
            dataRepository.loadStartData(deviceInfo)
                .onEach { CacheFileManager.saveAdv(it) }
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
                .collect { data ->
                    _advDataFlow.value = data.copy(advertiseType = advertiseType)
                    advId = bid?.id
                    advId?.let { listener.onLoadComplete(it) }

                }
        }
    }

    private fun parseAdvData(lurl: String?, vast: String) {
        VASTParser.setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {
                showListener.onShowError("", ShowErrorType.VIDEO_DATA_NOT_FOUND)
                callPixel(lurl ?: "")
            }

            override fun onVASTCacheError(error: Int) {
                showListener.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND)
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

    private fun callPixel(url: String) {
        dataRepository.callPixel(url)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(): Geo? {
        val manager = AdvSDK.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return if (ContextCompat.checkSelfPermission(
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
            Geo(utilLocation?.latitude, utilLocation?.longitude)
        } else {
            null
        }
    }

    fun playerLoadFinish() {
        callPixel(bid?.nurl ?: "")
    }

    fun showError(type: ShowErrorType, message: String = "") {
        advId?.let {
            showListener.onShowError(
                it,
                type,
                message
            )
        }

    }

    fun loadError(type: LoadErrorType, message: String = "") {
        loadListener.onLoadError(
            type,
            message
        )
    }

    fun loadSuccess() {
        advId?.let { loadListener.onLoadComplete(it) }
    }

    fun playerPlaybackFinish() {
        _advDataFlow.value = null
    }

    fun handleShowChangeState(state: ShowCompletionState) {
        advId?.let { showListener.onShowChangeState(it, state) }
    }

    @SuppressLint("HardwareIds")
    private fun makeDeviceInfo(
        isTestMode: Boolean,
        gameId: String,
        advReqType: AdvReqType = AdvReqType.VIDEO,
        advertiseType: AdvertiseType
    ): DeviceInfo {
        val geo = getLastLocation()
        return DeviceInfo(
            id = UUID.randomUUID().toString(),
            test = if (isTestMode) 1 else 0,
            listOf(
                when (advReqType) {
                    AdvReqType.VIDEO -> Imp(
                        id = "1",
                        video = Video(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(if (advertiseType == AdvertiseType.REWARDED) 1 else 0)
                        ),
                        instl = 1
                    )
                    AdvReqType.BANNER -> Imp(
                        id = "1",
                        banner = Banner(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(if (advertiseType == AdvertiseType.REWARDED) 1 else 0)
                        ),
                        instl = 1
                    )
                }
            ),
            AppInfo(
                gameId,
                AdvSDK.context.applicationInfo.loadLabel(AdvSDK.context.packageManager).toString(),
                AdvSDK.context.packageName
            ),
            Device(
                geo = geo ?: Geo(),
                deviceType = 0,
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


}



