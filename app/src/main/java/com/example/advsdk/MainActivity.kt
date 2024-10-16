package com.example.advsdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mobidriven.AdvSDK
import com.mobidriven.IAdHideBannerListener
import com.mobidriven.IAdInitializationListener
import com.mobidriven.IAdLoadListener
import com.mobidriven.IAdShowBannerListener
import com.mobidriven.IAdShowListener
import com.mobidriven.datasource.domain.model.*

class MainActivity : AppCompatActivity(), IAdShowListener, IAdShowBannerListener, IAdHideBannerListener, IAdInitializationListener {

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

        findViewById<EditText>(R.id.et_id).setText(MY_GAME_ID)


        findViewById<View>(R.id.btnInit).setOnClickListener {
            AdvSDK.initialize(this, findViewById<EditText>(R.id.et_id).text.toString(), true, this)
        }

        findViewById<View>(R.id.btnHideBanner).setOnClickListener {
            AdvSDK.hideBanner(null,object : IAdHideBannerListener{
                override fun onBannerHide(id: String?) {
//                    TODO("Not yet implemented")
                }

                override fun onBannerHideError(error: ShowErrorType, errorMessage: String, id: String?) {
//                    TODO("Not yet implemented")
                }

            } )
        }

        findViewById<View>(R.id.btnLoadRewarded).setOnClickListener {
            AdvSDK.load(AdvertiseType.REWARDED, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String?) {
                    addLog("REWARDED onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {
                    addLog("REWARDED onLoadError, id = $id, ${error.name} , errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnLoadInterstitial).setOnClickListener {
            AdvSDK.load(AdvertiseType.INTERSTITIAL, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String?) {
                    addLog("INTERSTITIAL onLoadComplete, id = $id")
                    Log.d("INTERSTITIAL", "onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {
                    addLog("INTERSTITIAL onLoadError, id = $id,   ${error.name} ,errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnLoadBanner).setOnClickListener {
            AdvSDK.load(AdvertiseType.BANNER, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String?) {
                    addLog("BANNER onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String?) {
                    addLog("BANNER onLoadError, id = $id, ${error.name} , errorMessage $errorMessage")
                }
            })
        }
        findViewById<View>(R.id.btnShowBanner).setOnClickListener {
            AdvSDK.showBanner(null, object : IAdShowBannerListener{
                override fun onBannerShow(id: String?) {
                    addLog("onBannerShow, id = $id")
                }

                override fun onBannerShowError(error: ShowErrorType, errorMessage: String, id: String?) {
                    addLog("onBannerShowError, id = $id errorMessage = ${error.name}")
                }



            })
        }
        /*findViewById<View>(R.id.btnLoadBanner3).setOnClickListener {
            AdvSDK.load(AdvertiseType.BANNER_480x320, listener = object : IAdLoadListener {
                override fun onLoadComplete(id: String) {
                    addLog("BANNER onLoadComplete, id = $id")
                }

                override fun onLoadError(error: LoadErrorType, errorMessage: String, id: String) {
                    addLog("BANNER onLoadError, id = $id, ${error.name} , errorMessage $errorMessage")
                }
            })
        }*/
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

    override fun onShowChangeState(id: String?, showCompletionState: ShowCompletionState) {
        addLog("onShowChangeState, id = $id showCompletionState = ${showCompletionState.name}")
    }


    override fun onShowError( error: ShowErrorType, errorMessage: String, id: String?) {
        addLog("onShowError, id = $id errorMessage = ${error.name}")
    }

    override fun onBannerShow(id: String?) {
        addLog("onBannerShow, id = $id")
    }

    override fun onBannerShowError(error: ShowErrorType, errorMessage: String, id: String?) {
        addLog("onBannerShowError, id = $id errorMessage = ${error.name}")
    }

    override fun onBannerHide(id: String?) {
        addLog("onBannerHide, id = $id")
    }

    override fun onBannerHideError(error: ShowErrorType, errorMessage: String, id: String?) {
        addLog("onBannerHideError, id = $id errorMessage = ${error.name}")
    }
}

//private const val MY_GAME_ID: String = "cd0f59937cd9b40710f11a39ee4e7636c263d56f" //cross test
//private const val MY_GAME_ID: String = "f4169c9d0e71da08ce0e98430632db404331d5e7" //cross test
//private const val MY_GAME_ID: String = "029b82b478b82eab4bdb13463765b5d6c3eef30f" //mraid banner
//private const val MY_GAME_ID: String = "secret" //mraid banner
//private const val MY_GAME_ID: String = "bf997a85569a0c697a06555119e32dcea4475d2d" //mraid banner
//private const val MY_GAME_ID: String = "9889865ad2c84f4d1d61605ffe3830e31e634e63" //video horizontal
//private const val MY_GAME_ID: String = "05b65b3909bacc5f2036d75e4f2b44b58861c4f4" //video horizontal
//private const val MY_GAME_ID: String = "18551279cbb7c79e083ab73bda57238783ef99da" //video horizontal rew
//private const val MY_GAME_ID: String = "b0492893722e4112dc0fb8e23cf978e4245ea075" //video horizontal
//private const val MY_GAME_ID: String = "f4169c9d0e71da08ce0e98430632db404331d5e7" //video horizontal
//private const val MY_GAME_ID: String = "bf997a85569a0c697a06555119e32dcea4475d2d" //mraid
//private const val MY_GAME_ID: String = "cc6cc257e6238a1a925fd6fb294bbd5e41693dcd"
//private const val MY_GAME_ID: String = "fe5a8f73f923a46f75586ee384530114e13c2b6d" //320x50 code
private const val MY_GAME_ID: String = "eab360c153374c950f4e7e3ba7325d9c141cf0f6" //320x50
//private const val MY_GAME_ID: String = "f4169c9d0e71da08ce0e98430632db404331d5e7" //320x480

//private const val MY_GAME_ID: String = "169a4d49448f9453c716c1daee05763d400747e4" //480х320
private const val AD_SERVER_HOST = "https://sp.mobidriven.com"