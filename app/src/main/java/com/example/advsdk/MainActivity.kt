package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.IAdShowListener
import com.mobileadvsdk.datasource.domain.model.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnStart).setOnClickListener {
            AdNetworkSDK.initialize(
                MY_GAME_ID,
                AD_SERVER_HOST,
                true,
                object : IAdInitializationListener {
                    override fun onInitializationComplete() {

                    }

                    override fun onInitializationError(
                        error: InitializationErrorType,
                        errorMessage: String
                    ) {
                        Log.e("onInitializationError", errorMessage)
                    }

                })
            AdNetworkSDK.load(AdvertiseType.REWARDED, object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    show()
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {

                }

            })
        }
    }
}

private fun show() {
    AdNetworkSDK.show("", object : IAdShowListener {
        override fun onShowComplete(id: String, showCompletionState: ShowCompletionState) {

        }

        override fun onShowError(id: String, error: ShowErrorType, errorMessage: String) {

        }
    })
}


private const val MY_GAME_ID: String = "MY_GAME_ID"
private const val AD_SERVER_HOST = "https://sp.mobidriven.com"