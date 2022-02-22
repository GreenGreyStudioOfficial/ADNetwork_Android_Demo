package com.mobileadvsdk.presentation

import android.app.AlertDialog
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.FragmentActivity
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.observe
import com.mobileadvsdk.presentation.player.VASTPlayer
import kotlinx.android.synthetic.main.activity_adv.*
import kotlinx.android.synthetic.main.dialog_close_advert.view.*

internal class AdvActivity : FragmentActivity() {
    private val advViewModel: AdvViewModel? = AdvSDK.provider

    private lateinit var advData: AdvData

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        advViewModel?.let {
            observe(it.advDataLive) { data ->
                data?.let { advNewData ->
                    advData = advNewData
                }
            }
        }

        advViewModel?.vastModel?.let {
            vastPlayer.load(it)
        }
        initPlayerListener()
    }

    private fun getAdvData() = advData.seatbid[0].bid[0]
    private fun getAdvertiseType() =
        if (advData.advertiseType == AdvertiseType.REWARDED) AdvertiseType.REWARDED else AdvertiseType.INTERSTITIAL

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