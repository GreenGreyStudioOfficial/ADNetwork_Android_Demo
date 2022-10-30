package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.*

class ShowActivity : AppCompatActivity() {
    private val advSDK = AdvSDK.INSTANCE
    private lateinit var advId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAndLoadAdv { id ->
            advId = id
        }

        findViewById<View>(R.id.btnShow).setOnClickListener {

            advSDK.show(advId, object : IAdShowListener {
                override fun onShowChangeState(id: String, state: ShowCompletionState) {
                    Log.d("AdvSDK", "show change state, id = $id ${state.name}")
                }

                override fun onShowError(id: String, error: ShowErrorType, message: String) {
                    Log.d("AdvSDK", "show error, id = $id message = $message")
                }
            })
        }
    }

    private fun initAndLoadAdv(onError: ((String) -> Unit)? = null, onLoad: (String) -> Unit) {
        advSDK.initialize(this.application, MY_GAME_ID, true, object : IAdInitializationListener {
            override fun onInitializationComplete() {
                Log.d("AdvSDK", "initialization complete")
                advSDK.load(AdvertiseType.REWARDED, object : IAdLoadListener {
                    override fun onLoadComplete(id: String) {
                        Log.d("AdvSDK", "onLoadComplete, id = $id")
                        onLoad(id)
                    }

                    override fun onLoadError(error: LoadErrorType, message: String, id: String) {
                        Log.d("AdvSDK", "load error, id = $id, message $message")
                        onError?.invoke("load error, id = $id, message $message")
                    }
                })
            }

            override fun onInitializationError(error: InitializationErrorType, message: String) {
                Log.d("AdvSDK", "on initialization error ${error.name} $message")
                onError?.invoke("on initialization error $message")
            }
        })
    }
}

private const val MY_GAME_ID: String = "secret"