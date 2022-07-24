package com.mobileadvsdk.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.presentation.player.VASTPlayer
import kotlinx.android.synthetic.main.activity_adv.*

internal class WebviewActivity : Activity() {
    private val provider: AdvProviderImpl = AdvSDK.provider!!
    internal lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = MraidJsInjectingWebViewClient()
        webView.addJavascriptInterface(MraidController(this), "MraidController")
        webView.loadDataWithBaseURL("http://www.example.com/", provider.adm?:"", "text/html", "UTF-8", null)


    }

    override fun onBackPressed() {

    }

    private fun showCloseDialog() {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle(R.string.dialog_title)
            setMessage(R.string.dialog_subtitle)
            setCancelable(false)
            setPositiveButton(R.string.dialog_continue_watch) { p0, _ ->
                vastPlayer.play()
                p0.dismiss()
            }
            setNegativeButton(R.string.dialog_close) { p0, _ ->
                provider.handleShowChangeState(ShowCompletionState.CLOSE)
                vastPlayer.onSkipConfirm()
                p0.dismiss()
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private class MraidJsInjectingWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
            val mraidJsFileName = "mraid.js"
            return if (request.url.toString().endsWith(mraidJsFileName)) {
                view?.context?.assets?.open(mraidJsFileName)?.let { stream ->
                    WebResourceResponse("text/javascript", "UTF-8", stream)
                }
            } else null
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.evaluateJavascript("bridge.notifyReadyEvent()",null)
        }
    }
}

