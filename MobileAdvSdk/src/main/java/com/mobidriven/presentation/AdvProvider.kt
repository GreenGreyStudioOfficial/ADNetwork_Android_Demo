package com.mobidriven.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mobidriven.AdvSDK
import com.mobidriven.IAdHideBannerListener
import com.mobidriven.IAdLoadListener
import com.mobidriven.IAdShowBannerListener
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

    val advShowFlow: MutableStateFlow<ShowAdv?> = MutableStateFlow(null)
    private val _advDataFlow: MutableStateFlow<AdvData?> = MutableStateFlow(null)

    private val dataRepository: IDataRepository = DataRepositoryImpl()

    var vastModel: VASTModel? = null

    private val advData: AdvData?
        get() = _advDataFlow.asStateFlow().value ?: CacheFileManager.loadAdv(advId)

    private val bid
        get() = advData?.seatbid?.first()?.bid?.first()

    val bannerSize
        get() = bid?.w to bid?.h

    internal var advId: String? = null

    internal val advType: AdvertiseType
        get() = if (advData?.advertiseType == AdvertiseType.REWARDED) AdvertiseType.REWARDED else AdvertiseType.INTERSTITIAL

    internal val adm: String?
        get() = bid?.adm

    private val _permissionChanel: Channel<Pair<String, String>> = Channel()

    internal val permissionChanel: Flow<Pair<String, String>>
        get() = _permissionChanel.receiveAsFlow()

    lateinit var showListener: IAdShowListener
    lateinit var loadListener: IAdLoadListener
    lateinit var showBannerListener: IAdShowBannerListener
    lateinit var hideBannerListener: IAdHideBannerListener

    init {

        scope.launch(Dispatchers.IO) {
            try {
                val data = makeDeviceInfo(isTestMode = false, gameId = gameId, advertiseType = AdvertiseType.REWARDED)
                    .run { AdvInitData(device = device, user = user) }

                dataRepository.sendInitUserData(gameId, data)
            } catch (t: Throwable) {
                t.printStackTrace()
            }

        }
    }

    fun loadAdv(advertiseType: AdvertiseType, listener: IAdLoadListener) {
        loadListener = listener
        makeRequest(advertiseType, listener = listener)
    }

    fun showAdv(id: String?, adShowListener: IAdShowListener) {
        showListener = adShowListener
        advData?.let {
            val bid = it.seatbid.first().bid.first()
            advId = bid.id
//            Log.e("BID", "bid $bid")
            when {
                listOf(5, 6).contains(bid.api) || bid.adm.contains("<script>") && bid.adm.contains("mraid.js") -> _showMraid(advId ?: "")
                bid.adm.contains("<script>") || bid.adm.contains("<html>") || bid.adm.contains("<body>") -> _showWeb(advId ?: "")
                else -> _parseAdvData(advId, bid.lurl, bid.adm)
            }
        } ?: run {
            CacheFileManager.clearCache()
            showListener.onShowError( error =  ShowErrorType.ADV_CACHE_NOT_FOUND, errorMessage = "", id=id)
        }
    }

    private fun _showMraid(id: String) {
        scope.launch {
            advShowFlow.emit(ShowAdv.MraidFullScreenAdv(id))
        }
    }

    private fun _showWeb(id: String?) {
        scope.launch {
            advShowFlow.emit(ShowAdv.WebFullScreenAdv(id ?: ""))
        }
    }

    internal fun showBanner(id: String?, listener: IAdShowBannerListener) {
        showBannerListener = listener
        advData?.let {
            val bid = it.seatbid.first().bid.first()
            _showBanner(bid.id)
        } ?: run {
            CacheFileManager.clearCache()
            listener.onBannerShowError(ShowErrorType.ADV_CACHE_NOT_FOUND, "", id ?: "")
        }
    }

    internal fun hideBanner(id: String?, listener: IAdHideBannerListener) {
        hideBannerListener = listener
        scope.launch {
            advShowFlow.emit(ShowAdv.HideBannerAdv(id ?: ""))
        }
    }



    internal fun onBannerShow(id: String?) {
        showBannerListener.onBannerShow(id)
    }

    internal fun onBannerHide(id: String?) {
        hideBannerListener.onBannerHide(id)
    }

    internal fun onBannerHideError(id: String?, type:ShowErrorType){
        hideBannerListener.onBannerHideError(type, id = id)
    }

    private fun _showBanner(id: String?) {
        scope.launch {
            advShowFlow.emit(ShowAdv.BannerAdv(id ?: ""))
        }
    }


    fun downloadImageAndSave(url: String) {
        val isGranted = ContextCompat.checkSelfPermission(
            AdvSDK.application, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            scope.launch {
                loadImage(url)?.let { saveMediaToStorage(it) }
                Toast.makeText(AdvSDK.application, "Изображение успешно сохранено", Toast.LENGTH_SHORT).show()
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
        val ctx = AdvSDK.application
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
        listener: IAdLoadListener
    ) {

        scope.launch(Dispatchers.IO) {
            val deviceInfo = makeDeviceInfo(isTestMode, gameId, advertiseType)
            dataRepository.loadStartData(deviceInfo, gameId)
                .onEach { CacheFileManager.saveAdv(it) }
                .catch {
//                    Log.e("AdvProvider", "err $it")
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
                                    LoadErrorType.AVAILABLE_CREATIVE_NOT_FOUND,
                                    LoadErrorType.AVAILABLE_CREATIVE_NOT_FOUND.desc
                                )
                            }
                        }

                        else -> {

                        }
                    }
                }
                .collect { data ->
//                    Log.e("DATA", "${data.toJson()}")
                    _advDataFlow.value = data.copy(advertiseType = advertiseType)
                    advId = bid?.id
                    withContext(Dispatchers.Main) {
                        listener.onLoadComplete(advId)
                    }
                }
        }
    }

    private fun _parseAdvData(id: String?, lurl: String?, vast: String) {
        VASTParser.setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {
                showListener.onShowError(error = ShowErrorType.VIDEO_DATA_NOT_FOUND, id = id)
                callPixel(lurl ?: "")
            }

            override fun onVASTCacheError(error: Int) {
                showListener.onShowError(error = ShowErrorType.ADV_CACHE_NOT_FOUND, id=id)
            }

            override fun onVASTParserFinished(model: VASTModel?) {
                vastModel = model
                scope.launch {
                    advShowFlow.emit(ShowAdv.VideoAdv(id ?: ""))
                }
            }
        })
        scope.launch {
            VASTParser.parseVast(AdvSDK.application, vast)
        }
    }

    private fun callPixel(url: String) {
        dataRepository.callPixel(url)
    }

    fun playerLoadFinish() {
        callPixel(bid?.nurl ?: "")
    }

    fun showError(type: ShowErrorType, message: String = "") {
        showListener.onShowError(
            error = type,
            errorMessage = message,
            id = advId,
        )

    }

    fun loadError(type: LoadErrorType, message: String = "") {
        loadListener.onLoadError(
            error = type,
            errorMessage = message,
            id = advId,
        )
    }

    fun playerPlaybackFinish() {
        _advDataFlow.value = null
        CacheFileManager.clearCache()
    }

    fun handleShowChangeState(state: ShowCompletionState) {
        showListener.onShowChangeState(advId, state)
    }

    @SuppressLint("HardwareIds")
    private fun makeDeviceInfo(
        isTestMode: Boolean,
        gameId: String,
        advertiseType: AdvertiseType
    ): DeviceInfo {
        val device: Device = DeviceInformation.getDeviceInfo(AdvSDK.application).fromJson()
        return DeviceInfo(
            id = UUID.randomUUID().toString(),
            test = if (isTestMode) 1 else 0,
            imp = when (advertiseType) {
                AdvertiseType.INTERSTITIAL -> listOf(
                    Imp(
                        id = "1",
                        video = Video(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(0)
                        ),
                        banner = Banner(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(0)
                        ),
                        instl = 1,
                    ),
                )

                AdvertiseType.REWARDED -> listOf(
                    Imp(
                        id = "1",
                        video = Video(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(1)
                        ),
                        banner = Banner(
                            w = Resources.getSystem().displayMetrics.widthPixels,
                            h = Resources.getSystem().displayMetrics.heightPixels,
                            ext = Ext(1)
                        ),
                        instl = 1,
                    ),
                )

                AdvertiseType.BANNER -> listOf(
                    Imp(
                        id = "1",
                        banner = Banner(
                            w = 320,
                            h = 50,
                            ext = Ext(0),
                        ),
                        instl = 0,
                    ),
                )
            },
            app = AppInfo(
                gameId,
                AdvSDK.application.applicationInfo.loadLabel(AdvSDK.application.packageManager).toString(),
                AdvSDK.application.packageName
            ),
            device = device,
            user = User(Prefs.userId)
        )

    }


}

internal sealed class ShowAdv {
    data class VideoAdv(val id: String) : ShowAdv()
    data class MraidFullScreenAdv(val id: String) : ShowAdv()
    data class WebFullScreenAdv(val id: String) : ShowAdv()
    data class BannerAdv(val id: String) : ShowAdv()
    data class HideBannerAdv(val id: String) : ShowAdv()
}



