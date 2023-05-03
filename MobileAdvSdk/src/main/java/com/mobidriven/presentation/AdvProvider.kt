package com.mobidriven.presentation

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
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobidriven.AdvSDK
import com.mobidriven.BuildConfig
import com.mobidriven.IAdLoadListener
import com.mobidriven.IAdShowListener
import com.mobidriven.datasource.data.DataRepositoryImpl
import com.mobidriven.datasource.data.Prefs
import com.mobidriven.datasource.domain.IDataRepository
import com.mobidriven.datasource.domain.model.*
import com.mobidriven.deviceinfo.DeviceInformation
import com.mobidriven.presentation.player.VASTParser
import com.mobidriven.presentation.player.model.VASTModel
import com.mobidriven.presentation.player.processor.CacheFileManager
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

    private val dataRepository: IDataRepository = DataRepositoryImpl()

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

    init {

        scope.launch(Dispatchers.IO) {
            try {
                val data = makeDeviceInfo(isTestMode = false, gameId = gameId, advertiseType = AdvertiseType.REWARDED)
                    .run { AdvInitData(device = device, user = user) }

                dataRepository.sendInitUserData(gameId, data)
            }catch (t:Throwable){
                t.printStackTrace()
            }

        }
    }

    fun loadAvd(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        loadListener = listener
        makeRequest(advertiseType, listener = listener)
    }

    fun showAvd(id: String, adShowListener: IAdShowListener) {
        advId = id
        showListener = adShowListener
        advData?.let {
            val bid = it.seatbid.first().bid.first()
            if (listOf(5, 6).contains(bid.api)) {
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
                _permissionChanel.send(Manifest.permission.WRITE_EXTERNAL_STORAGE to url)
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
        advReqType: AdvReqType = AdvReqType.DEFAULT,
        listener: IAdLoadListener
    ) {

        scope.launch(Dispatchers.IO) {
            val deviceInfo = makeDeviceInfo(isTestMode, gameId, advReqType, advertiseType)
            dataRepository.loadStartData(deviceInfo, if (BuildConfig.DEBUG) "secret" else gameId)
                .onEach { CacheFileManager.saveAdv(it) }
                .catch {
                    Log.e("AdvProvider", "err $it")
                    when (it) {
                        is IOException -> {

                            it.printStackTrace()
                            withContext(Dispatchers.Main) {
                                listener.onLoadError(
                                    LoadErrorType.CONNECTION_ERROR,
                                    LoadErrorType.CONNECTION_ERROR.desc
                                )
                            }

                        }
                        is IllegalStateException -> {
                            it.printStackTrace()
                            withContext(Dispatchers.Main) {
                                listener.onLoadError(
                                    LoadErrorType.AVAILABLE_VIDEO_NOT_FOUND,
                                    LoadErrorType.AVAILABLE_VIDEO_NOT_FOUND.desc
                                )
                            }
                        }
                        else -> {

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
        advReqType: AdvReqType = AdvReqType.DEFAULT,
        advertiseType: AdvertiseType
    ): DeviceInfo {
        val device : Device = DeviceInformation.getDeviceInfo(AdvSDK.context).fromJson()
        return DeviceInfo(
            id = UUID.randomUUID().toString(),
            test = if (isTestMode) 1 else 0,
            listOf(
                when (advReqType) {
                    AdvReqType.DEFAULT -> Imp(
                        id = "1",
                        video = Video(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(if (advertiseType == AdvertiseType.REWARDED) 1 else 0)
                        ),
                        instl = 1,
                    )
                    AdvReqType.WEB -> Imp(
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
            device,
            User(Prefs.userId)
        )

    }
}



