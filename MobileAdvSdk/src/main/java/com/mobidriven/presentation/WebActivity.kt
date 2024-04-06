package com.mobidriven.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import com.mobidriven.AdvSDK
import com.mobidriven.AdvSDK.createAdHtml
import com.mobidriven.R
import com.mobidriven.datasource.domain.model.AdvertiseType
import com.mobidriven.datasource.domain.model.LoadErrorType
import com.mobidriven.datasource.domain.model.ShowCompletionState
import com.mobidriven.presentation.player.processor.CacheFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class WebActivity : Activity() {
    private val provider: AdvProviderImpl = AdvSDK.provider!!
    private lateinit var webView: WebView
    private var isRewardReceived: Boolean = false

    @SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_adv)

        webView = findViewById(R.id.webView)
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false;
//        webView.settings.userAgentString = "0"
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        val adHtml = createAdHtml(provider.adm ?: "")
        webView.loadDataWithBaseURL("https://mobidriven.com", adHtml, "text/html", "UTF-8", null)

        findViewById<ImageView>(R.id.close).setOnClickListener {
            if (provider.advType == AdvertiseType.REWARDED && !isRewardReceived) {
                showCloseDialog()
            } else {
                provider.handleShowChangeState(ShowCompletionState.CLOSE)
                provider.playerPlaybackFinish()
                finish()
            }
        }
    }



    private fun lockOrientation(orientations: MraidOrientations) {
        if (orientations == MraidOrientations.PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (orientations == MraidOrientations.LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
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
        /*if (provider.advType == AdvertiseType.REWARDED && !isRewardReceived) {
            showCloseDialog()
        } else {
            provider.handleShowChangeState(ShowCompletionState.CLOSE)
            provider.playerPlaybackFinish()
            finish()
        }*/
    }

    private fun currentOrientation(): MraidOrientations =
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) MraidOrientations.PORTRAIT
        else MraidOrientations.LANDSCAPE

}

