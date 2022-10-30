package com.example.advsdk

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.datasource.domain.model.*

class MainActivity : AppCompatActivity(), IAdInitializationListener, IAdLoadListener, IAdShowListener {
    var advId :  String? = null
    private lateinit var recyclerView: RecyclerView

    private val logsAdapter by lazy {
        LogsAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.rvLogs).apply {
            adapter = logsAdapter
        }

        findViewById<View>(R.id.btnInit).setOnClickListener {
            AdvSDK.INSTANCE.initialize(this.application, MY_GAME_ID,  true, this)
        }

        findViewById<View>(R.id.btnLoadRewarded).setOnClickListener {
            AdvSDK.INSTANCE.load(AdvertiseType.REWARDED,this)
        }
        findViewById<View>(R.id.btnLoadInterstitial).setOnClickListener {
            AdvSDK.INSTANCE.load(AdvertiseType.INTERSTITIAL, object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    advId = id
                    addLog("INTERSTITIAL onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String) {
                    addLog("INTERSTITIAL onLoadError, id = $id, ${error.name}, errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnShow).setOnClickListener {
            advId?.let {
                AdvSDK.INSTANCE.show(it, this)
            }
        }
    }

    private fun addLog(log: String) {
        logsAdapter.addLog(log)
        recyclerView.smoothScrollToPosition(logsAdapter.itemCount - 1)
    }

    override fun onInitializationComplete() {
        addLog("initialization complete")
    }

    override fun onInitializationError(error: InitializationErrorType, message: String) {
        addLog("initialization error = ${error.name}, $message")
    }

    override fun onShowChangeState(id: String, state: ShowCompletionState) {
        addLog("show change state, id = $id state = ${state.name}")
    }

    override fun onShowError(id: String, error: ShowErrorType, message: String) {
        addLog("show error, id = $id message = $message")
    }

    override fun onLoadComplete(id: String) {
        advId = id
        addLog("load complete, id = $id")
    }

    override fun onLoadError(error: LoadErrorType, message: String, id: String) {
        addLog("load error, id = $id, message $message")
    }
}

private const val MY_GAME_ID: String = "secret"