package com.mobidriven.presentation.banner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.annotation.Keep
import com.mobidriven.AdvSDK.createAdHtml
import com.mobidriven.AdvSDK.currentActivity
import com.mobidriven.AdvSDK.provider
import com.mobidriven.R
import com.mobidriven.datasource.domain.model.AdvertiseType
import com.mobidriven.datasource.domain.model.LoadErrorType
import com.mobidriven.datasource.domain.model.ShowCompletionState
import com.mobidriven.presentation.JsSdkEvent
import com.mobidriven.presentation.MraidController
import com.mobidriven.presentation.MraidOrientations
import com.mobidriven.presentation.MraidPlacementTypes
import com.mobidriven.presentation.MraidStates
import com.mobidriven.presentation.player.processor.CacheFileManager


@Keep
internal class AdvBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val SMS = false
    private val TEL = true
    private val CALENDAR = false
    private val STORE_PICTURE = true
    private val INLINE_VIDEO = false
    private val SDK = true

    private val webView: WebView
    private val mraidController = MraidController {
        Log.e("MRAID BANNER VIEW", "event $it" )
        when (it) {
            JsSdkEvent.Close, JsSdkEvent.Unload -> {
                provider?.handleShowChangeState(ShowCompletionState.CLOSE)
                provider?.playerPlaybackFinish()
                provider?.onBannerHide(provider?.advId)
            }
            is JsSdkEvent.ContentLoaded -> {
                if (it.value) {
//                    isLoaded = true
                } else provider?.loadError(
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
                context.startActivity(browserIntent)
            }
            is JsSdkEvent.PlayVideo -> {
                //TODO()
            }
            is JsSdkEvent.Resize -> {
                fireCurrentPositionChangeEvent(it.offsetX, it.offsetY, it.width, it.height)
            }
            is JsSdkEvent.RewardReceived -> {
//                isRewardReceived = it.value
            }
            is JsSdkEvent.SetExpandProperties -> {
                //TODO()
            }
            is JsSdkEvent.SetOrientationProperties -> {
                if (!it.allowOrientationChange) return@MraidController
                val currentOrientation = resources.configuration.orientation
                if (it.forceOrientation == "portrait" && currentOrientation != Configuration.ORIENTATION_PORTRAIT) {
                    currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                if (it.forceOrientation == "landscape" && currentOrientation != Configuration.ORIENTATION_LANDSCAPE) {
                    currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }
            is JsSdkEvent.StorePicture -> provider?.downloadImageAndSave(it.uri)
        }
    }

    init {
        gravity = Gravity.CENTER
        id = R.id.adv_banner_view

        webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = MraidJsInjectingWebViewClient(::loadFinished)
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(mraidController, "MraidController")
        webView.setBackgroundColor(Color.TRANSPARENT)

        val bannerSize = provider?.bannerSize
        val dp2px = getContext().resources.displayMetrics.density
//        Log.e("SIZE", "$bannerSize ${bannerSize?.first?.let { (it * dp2px).toInt() }} ${bannerSize?.second?.let { (it * dp2px).toInt() } }")

        val lp = LayoutParams(bannerSize?.first?.let { (it * dp2px).toInt() } ?: LayoutParams.WRAP_CONTENT , bannerSize?.second?.let { (it * dp2px).toInt() } ?: LayoutParams.WRAP_CONTENT)
//        val lp = LayoutParams(320  , 50)
//        val lp = LayoutParams(320*dp2px.toInt()  , 50*dp2px.toInt())
        addView(webView, lp)
        load()
    }

    fun load() {
        val adm = provider?.adm
        adm ?: return


        val adHtml = createAdHtml(adm)
        webView.loadDataWithBaseURL("https://mobidriven.com", adHtml, "text/html", "UTF-8", null)

    }


    private fun loadFinished() {
//        Log.e("WebviewActivity", "load finished")
        try {
            sendEventToJs("bridge.notifyReadyEvent()")
            changeState(MraidStates.DEFAULT)
            firePlacementTypeChangeEvent()
            fireSupportsChangeEvent()
            val orientations = currentOrientation()
            lockOrientation(orientations)
            fireCurrentAppOrientationChangeEvent(orientations)
//            fireCurrentPositionChangeEvent(0, 0, width, height)
            fireDefaultPositionChangeEvent();
            fireMaxSizeChangeEvent()
            fireScreenSizeChangeEvent()
            fireVisibilityChangeEvent()
            fireRewardedChangeEvent()
        }catch (thr:Throwable){
            Log.e("MRAID", thr.message ?: "something wrong")
        }

    }

    private fun currentOrientation(): MraidOrientations =
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MraidOrientations.PORTRAIT
        else MraidOrientations.LANDSCAPE

    private fun lockOrientation(orientations: MraidOrientations) {
        if (orientations == MraidOrientations.PORTRAIT) {
            currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (orientations == MraidOrientations.LANDSCAPE) {
            currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun fireRewardedChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({rewarded:${provider?.advType == AdvertiseType.REWARDED}})"
        sendEventToJs(msg)
    }

    private fun fireVisibilityChangeEvent(visible: Boolean = true) {
        val msg =
            "bridge.fireChangeEvent({viewable:$visible})"
        sendEventToJs(msg)
    }

    private fun fireScreenSizeChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({screenSize:{width:$width,height:$height}})"
        sendEventToJs(msg)
    }

    private fun fireMaxSizeChangeEvent() {
        val msg =
            "bridge.fireChangeEvent({maxSize:{width:$width,height:$height}})"
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
            "bridge.fireChangeEvent({defaultPosition: { x: 0, y: 0, width: $width, height: $height}})"
        sendEventToJs(msg)
    }

    private class MraidJsInjectingWebViewClient(val loadFinished: () -> Unit) : WebViewClient() {

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
            handler.proceed() // Ignore SSL certificate errors
        }
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            Log.e("shouldInterceptRequest", "request ${request.url}")
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

}