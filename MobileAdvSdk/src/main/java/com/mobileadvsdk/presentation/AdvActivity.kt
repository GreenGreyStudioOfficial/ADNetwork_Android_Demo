package com.mobileadvsdk.presentation

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.ShowCompletionState
import com.mobileadvsdk.datasource.domain.model.ShowErrorType
import com.mobileadvsdk.presentation.player.VASTPlayer

internal class AdvActivity : Activity() {
    private val provider: AdvProviderImpl = AdvSDK.provider!!
    private lateinit var vastPlayer : VASTPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adv)
        vastPlayer = findViewById(R.id.vastPlayer)

        provider.vastModel?.let {
            vastPlayer.load(it)
        }
        initPlayerListener()
    }

    private fun initPlayerListener() {
        vastPlayer.setListener(object : VASTPlayer.Listener {
            override fun onVASTPlayerLoadFinish() {
                provider.playerLoadFinish()
                vastPlayer.setType(provider.advType)
                vastPlayer.play()
            }

            override fun onVASTPlayerFail(exception: Exception?) {
                provider.showError(ShowErrorType.UNKNOWN, exception?.message ?: "")
            }

            override fun onVASTPlayerCacheNotFound() {
                provider.showError(ShowErrorType.VIDEO_CACHE_NOT_FOUND)
            }

            override fun onVASTPlayerPlaybackStart() {
                provider.handleShowChangeState(ShowCompletionState.START)
            }

            override fun onVASTPlayerPlaybackFinish() {
                provider.handleShowChangeState(ShowCompletionState.COMPLETE)
                provider.playerPlaybackFinish()
                vastPlayer.destroy()
                finish()
            }

            override fun onVASTPlayerOpenOffer() {
                provider.handleShowChangeState(ShowCompletionState.OFFER)
            }

            override fun onVASTPlayerOnFirstQuartile() {
                provider.handleShowChangeState(ShowCompletionState.FIRST_QUARTILE)
            }

            override fun onVASTPlayerOnMidpoint() {
                provider.handleShowChangeState(ShowCompletionState.MIDPOINT)
            }

            override fun onVASTPlayerOnThirdQuartile() {
                provider.handleShowChangeState(ShowCompletionState.THIRD_QUARTILE)
            }

            override fun onVASTPlayerClose(needToConfirm: Boolean) {
                if (needToConfirm) {
                    showCloseDialog()
                } else {
                    provider.handleShowChangeState(ShowCompletionState.CLOSE)
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
                provider.handleShowChangeState(ShowCompletionState.SKIP)
                provider.handleShowChangeState(ShowCompletionState.CLOSE)
                provider.playerPlaybackFinish()
                vastPlayer.onSkipConfirm()
                p0.dismiss()
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}