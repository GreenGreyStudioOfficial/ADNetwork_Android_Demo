package com.mobileadvsdk.presentation.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mobileadvsdk.R


internal class CountDownView : FrameLayout {
    private lateinit var progressBarView: ProgressBar
    private lateinit var progressTextView: TextView

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(ctx: Context) {
        val rootView = inflate(ctx, R.layout.pubnative_player_count_down, this)
        progressBarView = rootView.findViewById<View>(R.id.view_progress_bar) as ProgressBar
        progressTextView = rootView.findViewById<View>(R.id.view_progress_text) as TextView
        val makeVertical = RotateAnimation(0f, -90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        makeVertical.fillAfter = true
        progressBarView.startAnimation(makeVertical)
    }

    fun setProgress(currentMs: Int, totalMs: Int) {
        progressBarView.max = totalMs
        progressBarView.secondaryProgress = totalMs
        progressBarView.progress = currentMs
        val remainSec = totalMs - currentMs
        if (remainSec <= 0) {
            this.visibility = INVISIBLE
        }
        progressTextView.text = remainSec.toString()
    }
}