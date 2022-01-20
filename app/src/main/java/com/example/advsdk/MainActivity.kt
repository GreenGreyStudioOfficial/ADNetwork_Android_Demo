package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType

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
                        Log.e("onInitializationError", errorMessage.toString())
                    }

                })
        }
    }
}


private const val MY_GAME_ID: String = "MY_GAME_ID"
private const val AD_SERVER_HOST = "https://sp.mobidriven.com"