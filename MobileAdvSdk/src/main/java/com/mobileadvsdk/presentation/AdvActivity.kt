package com.mobileadvsdk.presentation

import android.app.AlertDialog
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
import kotlinx.android.synthetic.main.dialog_close_advert.view.*
import net.pubnative.player.AdvType
import net.pubnative.player.VASTPlayer
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.subKodein

class AdvActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein = subKodein(KodeinHolder.kodein) {}

    private val advViewModel: AdvViewModel? = AdNetworkSDK.provider

    private lateinit var advData: AdvData

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Log.e("SCREEN_SIZE", " " + displayMetrics.widthPixels)
        Log.e("SCREEN_SIZE", " " + displayMetrics.heightPixels)

        advViewModel?.let {
            observe(it.advDataLive) { data ->
                advData = data
            }
        }

        advViewModel?.vastModel?.let {
            vastPlayer.load(it)
        }
        initPlayerListener()
    }

    private fun getAdvData() = advData.seatbid[0].bid[0]
    private fun getAdvertiseType() =
        if (advData.advertiseType?.name == AdvType.REWARDED.name) AdvType.REWARDED else AdvType.INTERSTITIAL

    private fun getAdvId(): String = getAdvData().id ?: ""

    private fun handleShowChangeState(state: ShowCompletionState) =
        advViewModel?.iAdShowListener?.onShowChangeState(getAdvId(), state)

    private fun initPlayerListener() {
        vastPlayer.setListener(object : VASTPlayer.Listener {
            override fun onVASTPlayerLoadFinish() {
                advViewModel?.getUrl(getAdvData().nurl ?: "")
                vastPlayer.setType(getAdvertiseType())
                vastPlayer.play()
            }

            override fun onVASTPlayerFail(exception: Exception?) {
                Log.e("onVASTPlayerFail", exception?.localizedMessage, exception)
                advViewModel?.iAdShowListener?.onShowError(
                    getAdvId(),
                    ShowErrorType.UNKNOWN,
                    exception?.message ?: ""
                )
            }

            override fun onVASTPlayerCacheNotFound() {
                advViewModel?.iAdShowListener?.onShowError("", ShowErrorType.VIDEO_CACHE_NOT_FOUND, "")
            }

            override fun onVASTPlayerPlaybackStart() {
                handleShowChangeState(ShowCompletionState.START)
            }

            override fun onVASTPlayerPlaybackFinish() {
                handleShowChangeState(ShowCompletionState.COMPLETE)
                advViewModel?.advDataLive?.postValue(null)
                vastPlayer.destroy()
                finish()
            }

            override fun onVASTPlayerOpenOffer() {
                handleShowChangeState(ShowCompletionState.OFFER)
            }

            override fun onVASTPlayerOnFirstQuartile() {
                handleShowChangeState(ShowCompletionState.FIRST_QUARTILE)
            }

            override fun onVASTPlayerOnMidpoint() {
                handleShowChangeState(ShowCompletionState.MIDPOINT)
            }

            override fun onVASTPlayerOnThirdQuartile() {
                handleShowChangeState(ShowCompletionState.THIRD_QUARTILE)
            }

            override fun onVASTPlayerClose(needToConfirm: Boolean) {
                if (needToConfirm) {
                    showCloseDialog()
                } else {
                    handleShowChangeState(ShowCompletionState.CLOSE)
                    vastPlayer.onSkipConfirm()
                    advViewModel?.advDataLive?.postValue(null)
                    finish()
                }
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

    override fun onBackPressed() {
        vastPlayer.onSkipClick()
    }

    private fun showCloseDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_close_advert, null)
        builder.setView(view)
        builder.setCancelable(true)
        val dialog = builder.create()
        view.btnClose.setOnClickListener {
            handleShowChangeState(ShowCompletionState.CLOSE)
            vastPlayer.onSkipConfirm()
            dialog.dismiss()
            finish()
        }
        view.btnContinue.setOnClickListener {
            vastPlayer.play()
            dialog.dismiss()
        }
        dialog.show()
    }
}