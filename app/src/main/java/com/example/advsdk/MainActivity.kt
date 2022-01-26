package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.*

class MainActivity : AppCompatActivity() {

    private val logsAdapter by lazy {
        LogsAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.rvLogs).adapter = logsAdapter

        findViewById<View>(R.id.btnInit).setOnClickListener {
            AdNetworkSDK.initialize(
                MY_GAME_ID,
                AD_SERVER_HOST,
                true,
                object : IAdInitializationListener {
                    override fun onInitializationComplete() {
                        logsAdapter.addLog("onInitializationComplete")
                    }

                    override fun onInitializationError(
                        error: InitializationErrorType,
                        errorMessage: String
                    ) {
                        Log.e("onInitializationError", errorMessage)
                        logsAdapter.addLog("onInitializationError = $errorMessage")
                    }
                })
        }
        findViewById<View>(R.id.btnLoadRewarded).setOnClickListener {
            AdNetworkSDK.load(AdvertiseType.REWARDED, object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    logsAdapter.addLog("REWARDED onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {
                    logsAdapter.addLog("REWARDED onLoadError, id = $id, errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnLoadInterstitial).setOnClickListener {
            AdNetworkSDK.load(AdvertiseType.INTERSTITIAL, object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    logsAdapter.addLog("INTERSTITIAL onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {
                    logsAdapter.addLog("INTERSTITIAL onLoadError, id = $id, errorMessage $errorMessage")
                }

            })
        }

        findViewById<View>(R.id.btnShow).setOnClickListener {
            AdNetworkSDK.show("", object : IAdShowListener {
                override fun onShowChangeState(
                    id: String,
                    showCompletionState: ShowCompletionState
                ) {
                    logsAdapter
                        .addLog("onShowError, id = $id showCompletionState = ${showCompletionState.name}")
                }

                override fun onShowError(id: String, error: ShowErrorType, errorMessage: String) {
                    logsAdapter.addLog("onShowError, id = $id errorMessage = $errorMessage")
                }
            })
        }
    }
}

private const val MY_GAME_ID: String = "MY_GAME_ID"
private const val AD_SERVER_HOST = "https://sp.mobidriven.com"