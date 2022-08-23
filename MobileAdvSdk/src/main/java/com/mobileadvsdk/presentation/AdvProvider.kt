package com.mobileadvsdk.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
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

    private val _permissionChanel: Channel<Pair<String, String>> = Channel()

    internal val permissionChanel: Flow<Pair<String, String>>
        get() = _permissionChanel.receiveAsFlow()

    lateinit var showListener: IAdShowListener
    lateinit var loadListener: IAdLoadListener

    fun loadAvd(advertiseType: AdvertiseType, advReqType: AdvReqType = AdvReqType.VIDEO, listener: IAdLoadListener) {
        loadListener = listener
        makeRequest(advertiseType, advReqType, listener = listener)
    }

    fun showAvd(id: String, adShowListener: IAdShowListener) {
        advId = id
        showListener = adShowListener
        advData?.let {
            val bid = it.seatbid.first().bid.first()
            if (listOf(5, 6).contains(bid.api) || bid.adm.startsWith("<!DOCTYPE html>")) {
                showMraid(bid.id)
            } else {
                parseAdvData(bid.lurl, bid.adm)
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

    fun downloadImageAndSave(url: String) {
        val isGranted = ContextCompat.checkSelfPermission(
            AdvSDK.context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            scope.launch {
                loadImage(url)?.let { saveMediaToStorage(it) }
                Toast.makeText(AdvSDK.context, "Изображение успешно сохранено", Toast.LENGTH_SHORT).show()
            }
        } else {
            scope.launch {
                _permissionChanel.send(Manifest.permission.WRITE_EXTERNAL_STORAGE to url )
            }
        }

    }

    private suspend fun loadImage(stringUrl: String): Bitmap? {
        val url = stringToURL(stringUrl) ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "Promo.jpg"
        var fos: OutputStream? = null
        val ctx = AdvSDK.context
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ctx.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                fos = FileOutputStream(File(imagesDir, filename))
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        }
    }

    private fun stringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun makeRequest(
        advertiseType: AdvertiseType,
        advReqType: AdvReqType,
        listener: IAdLoadListener
    ) {
        val deviceInfo = makeDeviceInfo(isTestMode, gameId, advReqType, advertiseType)

        scope.launch(Dispatchers.IO) {
            dataRepository.loadStartData(deviceInfo, "secret")
                .onEach { CacheFileManager.saveAdv(it) }
                .catch {
                    when (it) {
                        is IOException -> {
//                            Log.e("AdvProvider", "err $it")
                            it.printStackTrace()
                            withContext(Dispatchers.Main) {
                                listener.onLoadError(
                                    LoadErrorType.CONNECTION_ERROR,
                                    LoadErrorType.CONNECTION_ERROR.desc
                                )
                            }

                        }
                    }
                }
                .collect { data ->
                    _advDataFlow.value = data.copy(advertiseType = advertiseType)
                    advId = bid?.id
                    withContext(Dispatchers.Main) {
                        advId?.let { listener.onLoadComplete(it) }
                    }
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

    fun playerPlaybackFinish() {
        _advDataFlow.value = null
        CacheFileManager.clearCache()
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
                        instl = 1,
                    )
                    AdvReqType.BANNER -> Imp(
                        id = "1",
                        banner = Banner(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(if (advertiseType == AdvertiseType.REWARDED) 1 else 0)
                        ),
                        instl = 1,
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } else {
            val mInfo = cm.activeNetworkInfo
            if (mInfo == null || !mInfo.isConnected) return 0
            if (mInfo.type == ConnectivityManager.TYPE_ETHERNET) return 1
            if (mInfo.type == ConnectivityManager.TYPE_WIFI) return 2
            if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return when (mInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> 4
                    TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> 5
                    TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> 6
                    TelephonyManager.NETWORK_TYPE_NR -> 6
                    else -> 3
                }
            }
            return 0
        }
    }
}



