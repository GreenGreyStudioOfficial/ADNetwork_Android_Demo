package com.example.advsdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.IAdInitializationListener
import com.mobileadvsdk.IAdLoadListener
import com.mobileadvsdk.datasource.domain.model.AdvReqType
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.InitializationErrorType
import com.mobileadvsdk.datasource.domain.model.LoadErrorType

class LoadActivity : AppCompatActivity() {
    lateinit var advId :  String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        afterSdkInit {
            AdvSDK.INSTANCE.load(AdvertiseType.REWARDED,object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    advId = id
                    Log.d("AdvSDK", "REWARDED onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String) {
                    Log.d("AdvSDK", "REWARDED onLoadError, id = $id, errorMessage $errorMessage")
                }
            })
        }
    }

    private fun afterSdkInit(onError:((String)->Unit)? = null, onInit :() -> Unit){
        AdvSDK.INSTANCE.initialize(this.application, MY_GAME_ID, true, object : IAdInitializationListener {
            override fun onInitializationComplete() {
                Log.d("AdvSDK", "initialization complete")
                onInit()
            }

            override fun onInitializationError(error: InitializationErrorType, message: String) {
                Log.d("AdvSDK", "on initialization error ${error.name} $message")
                onError?.invoke("on initialization error $message")
            }
        })
    }
}

private const val MY_GAME_ID: String = "secret"