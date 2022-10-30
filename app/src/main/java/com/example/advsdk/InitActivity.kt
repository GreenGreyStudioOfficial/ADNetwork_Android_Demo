package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType

class InitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        AdvSDK.INSTANCE.initialize(this.application, MY_GAME_ID, true, object : IAdInitializationListener {
            override fun onInitializationComplete() {
                Log.d("AdvSDK", "initialization complete")
            }

            override fun onInitializationError(error: InitializationErrorType, message: String) {
                Log.d("AdvSDK", "on initialization error ${error.name} $message")
            }
        })
    }
}

private const val MY_GAME_ID: String = "secret"