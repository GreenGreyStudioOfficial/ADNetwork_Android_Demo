package com.example.advsdk

import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.*

class MainActivity : AppCompatActivity(), IAdInitializationListener, IAdShowListener {

    private lateinit var recyclerView: RecyclerView

    private val logsAdapter by lazy {
        LogsAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.rvLogs).apply {
            adapter = logsAdapter
        }

        findViewById<View>(R.id.btnInit).setOnClickListener {
            AdvSDK.initialize(this.application, MY_GAME_ID,  true, this)
        }

        findViewById<View>(R.id.btnLoadRewarded).setOnClickListener {
            AdvSDK.load(AdvertiseType.REWARDED, AdvReqType.WEB, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    addLog("REWARDED onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String) {
                    addLog("REWARDED onLoadError, id = $id, ${error.name} , errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnLoadInterstitial).setOnClickListener {
            AdvSDK.load(AdvertiseType.INTERSTITIAL, AdvReqType.WEB, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    addLog("INTERSTITIAL onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String) {
                    addLog("INTERSTITIAL onLoadError, id = $id,   ${error.name} ,errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnShow).setOnClickListener {
            AdvSDK.show("", this)
        }
    }

    private fun addLog(log: String) {
        logsAdapter.addLog(log)
        recyclerView.smoothScrollToPosition(logsAdapter.itemCount - 1)
    }

    override fun onInitializationComplete() {
        addLog("onInitializationComplete")
    }

    override fun onInitializationError(error: InitializationErrorType, errorMessage: String) {
        addLog("onInitializationError = ${error.name}, $errorMessage")
    }

    override fun onShowChangeState(id: String, showCompletionState: ShowCompletionState) {
        addLog("onShowChangeState, id = $id showCompletionState = ${showCompletionState.name}")
    }

    override fun onShowError(id: String, error: ShowErrorType, errorMessage: String) {
        addLog("onShowError, id = $id errorMessage = ${error.name}")
    }
}

private const val MY_GAME_ID: String = "secret"
private const val AD_SERVER_HOST = "https://sp.mobidriven.com"