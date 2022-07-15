package com.mobileadvsdk.presentation

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Window
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.presentation.player.VASTPlayer
import kotlinx.android.synthetic.main.activity_adv.*

internal class AdvActivity : Activity() {
    private val provider: AdvProviderImpl = AdvSDK.provider!!
    private val advData
        get() = provider.advData

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)

        provider?.vastModel?.let {
            vastPlayer.load(it)
        }
        initPlayerListener()
    }

    private fun getBid() = advData?.seatbid?.firstOrNull()?.bid?.firstOrNull()
    private fun getAdvertiseType() =
        if (advData?.advertiseType == AdvertiseType.REWARDED) AdvertiseType.REWARDED else AdvertiseType.INTERSTITIAL

    private fun getAdvId(): String = getBid()?.id ?: ""

    private fun handleShowChangeState(state: ShowCompletionState) =
        provider.iAdShowListener.onShowChangeState(getAdvId(), state)

    private fun initPlayerListener() {
        vastPlayer.setListener(object : VASTPlayer.Listener {
            override fun onVASTPlayerLoadFinish() {
                provider.playerLoadFinish()
                vastPlayer.setType(getAdvertiseType())
                vastPlayer.play()
            }

            override fun onVASTPlayerFail(exception: Exception?) {
                provider.showError(ShowErrorType.UNKNOWN, exception?.message ?: "")
            }

            override fun onVASTPlayerCacheNotFound() {
                provider.showError(ShowErrorType.VIDEO_CACHE_NOT_FOUND)
            }

            override fun onVASTPlayerPlaybackStart() {
                handleShowChangeState(ShowCompletionState.START)
            }

            override fun onVASTPlayerPlaybackFinish() {
                handleShowChangeState(ShowCompletionState.COMPLETE)
                provider.playerPlaybackFinish()
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
                    provider.playerPlaybackFinish()
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
        with(builder) {
            setTitle(R.string.dialog_title)
            setMessage(R.string.dialog_subtitle)
            setCancelable(false)
            setPositiveButton(R.string.dialog_continue_watch) { p0, _ ->
                vastPlayer.play()
                p0.dismiss()
            }
            setNegativeButton(R.string.dialog_close) { p0, _ ->
                handleShowChangeState(ShowCompletionState.CLOSE)
                vastPlayer.onSkipConfirm()
                p0.dismiss()
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}