package com.mobileadvsdk.presentation

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.di.KodeinHolder
import com.mobileadvsdk.observe
import kotlinx.android.synthetic.main.activity_adv.*
import net.pubnative.player.VASTParser
import net.pubnative.player.VASTPlayer
import net.pubnative.player.model.VASTModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.subKodein


class AdvActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein = subKodein(KodeinHolder.kodein) {}

    private val advViewMadel: AdvViewModel? = AdNetworkSDK.provider

    private lateinit var advData: AdvData

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Log.e("SCREEN_SIZE", " " + displayMetrics.widthPixels)
        Log.e("SCREEN_SIZE", " " + displayMetrics.heightPixels)

        advViewMadel?.let {
            observe(it.advDataLive) {
                advData = it
                parseAdvData(it)
            }
        }
        initPlayerListener()
    }

    private fun parseAdvData(it: AdvData) {
        VASTParser(this).setListener(object : VASTParser.Listener {
            override fun onVASTParserError(error: Int) {

            }

            override fun onVASTCacheError(error: Int) {
                TODO("Not yet implemented")
            }

            override fun onVASTParserFinished(model: VASTModel) {
                vastPlayer.load(model)
            }
        }).execute(it.seatbid[0].bid[0].adm)
    }

    private fun getAdvId(): String = advData.seatbid[0].bid[0].id ?: ""

    private fun initPlayerListener() {
        vastPlayer.setListener(object : VASTPlayer.Listener {
            override fun onVASTPlayerLoadFinish() {
                advViewMadel?.getUrl(advData.seatbid[0].bid[0].nurl ?: "")
                vastPlayer.play()
            }

            override fun onVASTPlayerFail(exception: Exception?) {
                Log.e("onVASTPlayerFail", exception?.localizedMessage, exception)
                advViewMadel?.iAdShowListener?.onShowError(
                    getAdvId(),
                    ShowErrorType.UNKNOWN,
                    exception?.message ?: ""
                )
            }

            override fun onVASTPlayerPlaybackStart() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.START
                )
            }

            override fun onVASTPlayerPlaybackFinish() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.COMPLETE
                )
            }

            override fun onVASTPlayerOpenOffer() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.OFFER
                )
            }

            override fun onVASTPlayerOnFirstQuartile() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.FIRST_QUARTILE
                )
            }

            override fun onVASTPlayerOnMidpoint() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.MIDPOINT
                )
            }

            override fun onVASTPlayerOnThirdQuartile() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.THIRD_QUARTILE
                )
            }

            override fun onVASTPlayerClose() {
                advViewMadel?.iAdShowListener?.onShowChangeState(
                    getAdvId(),
                    ShowCompletionState.CLOSE
                )
            }
        })
    }

    override fun onPause() {
        super.onPause()
        vastPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        vastPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        vastPlayer.destroy()
    }
}