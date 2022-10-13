package com.mobileadvsdk.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.LoadErrorType
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.presentation.player.processor.CacheFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class WebviewActivity : Activity() {
    private val provider: AdvProviderImpl = AdvSDK.provider!!
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private var isRewardReceived: Boolean = false
    private var isLoaded: Boolean = false

    private val displayMetrics by lazy { resources.displayMetrics }
    private val displayWidth by lazy { displayMetrics.widthPixels }
    private val displayHeight by lazy { displayMetrics.heightPixels }

    private val mraidController = MraidController {
        when (it) {
            JsSdkEvent.Close, JsSdkEvent.Unload -> {
                provider.handleShowChangeState(ShowCompletionState.CLOSE)
                provider.playerPlaybackFinish()
                finish()
            }
            is JsSdkEvent.ContentLoaded -> {
                if (it.value) {
                    isLoaded = true
                    webView.visibility = View.VISIBLE
                } else provider.loadError(
                    LoadErrorType.WEBVIEW_CONTENT_NOT_LOADED,
                    LoadErrorType.WEBVIEW_CONTENT_NOT_LOADED.desc
                )
            }
            JsSdkEvent.CreateCalendarEvent -> {
                //TODO()
            }
            is JsSdkEvent.Expand -> {
                //TODO()
            }
            is JsSdkEvent.Open -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.uri))
                startActivity(browserIntent)
            }
            is JsSdkEvent.PlayVideo -> {
                //TODO()
            }
            is JsSdkEvent.Resize -> {
                fireCurrentPositionChangeEvent(it.offsetX, it.offsetY, it.width, it.height)
            }
            is JsSdkEvent.RewardReceived -> {
                isRewardReceived = it.value
            }
            is JsSdkEvent.SetExpandProperties -> {
                //TODO()
            }
            is JsSdkEvent.SetOrientationProperties -> {
                if (!it.allowOrientationChange) return@MraidController
                val currentOrientation = resources.configuration.orientation
                if (it.forceOrientation == "portrait" && currentOrientation != Configuration.ORIENTATION_PORTRAIT) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                if (it.forceOrientation == "landscape" && currentOrientation != Configuration.ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }
            is JsSdkEvent.StorePicture -> provider.downloadImageAndSave(it.uri)
        }
    }

    private val SMS = false
    private val TEL = true
    private val CALENDAR = false
    private val STORE_PICTURE = true
    private val INLINE_VIDEO = false
    private val SDK = true
    private val EXTERNAL_STORAGE_PERMISSION_CODE = 767

    private var downloadImageUrl :String? = null


    @SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_adv)
        webView = findViewById(R.id.webView)
        progress = findViewById(R.id.progressBar)
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false;
//        webView.settings.userAgentString = "0"
        webView.webViewClient = MraidJsInjectingWebViewClient(::loadFinished)
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(mraidController, "MraidController")
        webView.loadDataWithBaseURL("https://mobidriven.com", provider.adm ?: "", "text/html", "UTF-8", null)

        AdvSDK.scope.launch(Dispatchers.IO) {
            provider.permissionChanel.collect {
                downloadImageUrl = it.second
                ActivityCompat.requestPermissions(
                    this@WebviewActivity,
                    arrayOf(it.first),
                    EXTERNAL_STORAGE_PERMISSION_CODE
                );
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                downloadImageUrl?.let { provider.downloadImageAndSave(it) }
            } else{
                downloadImageUrl = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isLoaded) fireVisibilityChangeEvent(true)
    }

    override fun onPause() {
        super.onPause()
        fireVisibilityChangeEvent(false)
    }

    private fun loadFinished() {
//        Log.e("WebviewActivity", "load finished")
        sendEventToJs("bridge.notifyReadyEvent()")
        changeState(MraidStates.DEFAULT)
        firePlacementTypeChangeEvent()
        fireSupportsChangeEvent()
        val orientations = currentOrientation()
        lockOrientation(orientations)
        fireCurrentAppOrientationChangeEvent(orientations)
        fireCurrentPositionChangeEvent(0, 0, displayWidth, displayHeight)
        fireDefaultPositionChangeEvent();
        fireMaxSizeChangeEvent()
        fireScreenSizeChangeEvent()
        fireVisibilityChangeEvent()
        fireRewardedChangeEvent()
    }

    private fun lockOrientation(orientations: MraidOrientations) {
        if (orientations == MraidOrientations.PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (orientations == MraidOrientations.LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun fireRewardedChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({rewarded:${provider.advType == AdvertiseType.REWARDED}})"
        sendEventToJs(msg)
    }

    private fun fireVisibilityChangeEvent(visible: Boolean = true) {
        val msg =
            "bridge.fireChangeEvent({viewable:$visible})"
        sendEventToJs(msg)
    }

    private fun fireScreenSizeChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({screenSize:{width:$displayWidth,height:$displayHeight}})"
        sendEventToJs(msg)
    }

    private fun fireMaxSizeChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({maxSize:{width:$displayWidth,height:$displayHeight}})"
        sendEventToJs(msg)
    }

    private fun sendEventToJs(event: String) {
        webView.evaluateJavascript(event, null)
    }

    private fun changeState(state: MraidStates) {
        sendEventToJs("bridge.fireChangeEvent({state:${state.event}})")
    }

    private fun fireSupportsChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({supports:{sms:$SMS,tel:$TEL,calendar:$CALENDAR,storePicture:$STORE_PICTURE,inlineVideo:$INLINE_VIDEO,sdk:$SDK}})"
        sendEventToJs(msg)
    }

    private fun fireCurrentAppOrientationChangeEvent(mraidOrientation: MraidOrientations = MraidOrientations.PORTRAIT) {
        val msg =
            "bridge.fireChangeEvent({currentAppOrientation:{orientation:${mraidOrientation.event}, locked:true}})"
        sendEventToJs(msg)
    }

    private fun firePlacementTypeChangeEvent(type: MraidPlacementTypes = MraidPlacementTypes.INTERSTITIAL) {
        val msg = "bridge.fireChangeEvent({placementType: ${type.event}})"
        sendEventToJs(msg)
    }

    private fun fireCurrentPositionChangeEvent(x: Int, y: Int, width: Int, height: Int) {
        val msg = "bridge.fireChangeEvent({currentPosition: { x: $x, y: $y, width: $width, height: $height}})"
        sendEventToJs(msg)
    }

    private fun fireDefaultPositionChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({defaultPosition: { x: 0, y: 0, width: $displayWidth, height: $displayHeight}})"
        sendEventToJs(msg)
    }

    private fun showCloseDialog() {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle(R.string.dialog_title)
            setMessage(R.string.dialog_subtitle)
            setCancelable(false)
            setPositiveButton(R.string.dialog_continue_watch) { p0, _ ->
                p0.dismiss()
            }
            setNegativeButton(R.string.dialog_close) { p0, _ ->
                provider.handleShowChangeState(ShowCompletionState.SKIP)
                provider.handleShowChangeState(ShowCompletionState.CLOSE)
                provider.playerPlaybackFinish()
                p0.dismiss()
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        if (provider.advType == AdvertiseType.REWARDED && !isRewardReceived) {
            showCloseDialog()
        } else {
            provider.handleShowChangeState(ShowCompletionState.CLOSE)
            provider.playerPlaybackFinish()
            finish()
        }
    }

    private class MraidJsInjectingWebViewClient(val loadFinished: () -> Unit) : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            return when {
                request.url.toString().endsWith("mraid.js") -> view?.context?.assets?.open("mraid.js")
                    ?.let { stream ->
                        WebResourceResponse("text/javascript", "UTF-8", stream)
                    }

                request.url.toString().endsWith(".mp4") -> CacheFileManager.getCacheResourceFile(request.url.toString())
                    ?.let { stream -> WebResourceResponse("video/mp4", "UTF-8", stream) }

                request.url.toString().endsWith(".png") -> CacheFileManager.getCacheResourceFile(request.url.toString())
                    ?.let { stream -> WebResourceResponse("image/png", "UTF-8", stream) }

                request.url.toString().endsWith(".jpg") -> CacheFileManager.getCacheResourceFile(request.url.toString())
                    ?.let { stream -> WebResourceResponse("image/jpg", "UTF-8", stream) }

                else -> {
                    super.shouldInterceptRequest(view, request)
                }
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loadFinished()
        }
    }

    private fun currentOrientation(): MraidOrientations =
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MraidOrientations.PORTRAIT
        else MraidOrientations.LANDSCAPE

}

internal enum class MraidStates(val event: String) {
    LOADING("mraid.STATES.LOADING"),
    DEFAULT("mraid.STATES.DEFAULT"),
    EXPANDED("mraid.STATES.EXPANDED"),
    HIDDEN("mraid.STATES.HIDDEN"),
    RESIZED("mraid.STATES.RESIZED")
}

internal enum class MraidPlacementTypes(val event: String) {
    UNKNOWN("mraid.PLACEMENT_TYPES.UNKNOWN"),
    INLINE("mraid.PLACEMENT_TYPES.INLINE"),
    INTERSTITIAL("mraid.PLACEMENT_TYPES.INTERSTITIAL"),
}

internal enum class MraidOrientations(val event: String) {
    LANDSCAPE("landscape"),
    PORTRAIT("portrait"),
    NONE("none"),
}

