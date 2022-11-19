package com.mobidriven.presentation.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mobidriven.R


internal class CountDownView : FrameLayout {
    //    private lateinit var progressBarView: ProgressBar
    private lateinit var progressBarView: SquareProgressView
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
//        progressBarView = rootView.findViewById<View>(R.id.view_progress_bar) as ProgressBar
        progressBarView = rootView.findViewById<View>(R.id.view_progress_bar) as SquareProgressView
        progressTextView = rootView.findViewById<View>(R.id.view_progress_text) as TextView
        val makeVertical = RotateAnimation(0f, -90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        makeVertical.fillAfter = true
        progressBarView.startAnimation(makeVertical)
    }

    fun setProgress(currentMs: Int, totalMs: Int) {
        val total = totalMs / 1000
        progressBarView.maxValue = total
        progressBarView.secondaryProgress = total
        progressBarView.setProgress(currentMs.toFloat())
        val remainSec = total - currentMs
        if (remainSec <= 0) {
            this.visibility = INVISIBLE
        }
        progressTextView.text = remainSec.toString()
    }
}


internal class SquareProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {


    var secondaryProgress: Int = 0
    private val progressColor = Color.BLACK
    private val backgroundColor = Color.WHITE
    var maxValue = 100
    private var startingAngle = 0

    private lateinit var progressBarPaint: Paint
    private lateinit var backgroundPaint: Paint
    private var mRadius = 0f
    private val mArcBounds = RectF()
    private val mBounds = RectF()
    private val mInnerBounds = RectF()
    private val clipPath = Path()
    private var drawUpto = 0f
    private var dpToPx = 2f


    init {
        initPaints(context)
    }

    fun setProgress(value: Float) {
        this.drawUpto = value
        Log.e("CountDownView", "drawUpto")
        invalidate()
    }


    private fun initPaints(context: Context) {
        progressBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressBarPaint.style = Paint.Style.FILL
        progressBarPaint.color = progressColor
        dpToPx = resources.displayMetrics.density
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = backgroundColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = Math.min(w, h) / 2f
        mArcBounds.set(-8 * dpToPx, -8 * dpToPx, 2 * mRadius + 8 * dpToPx, 2 * mRadius + 8 * dpToPx)
        mBounds.set(0f,0f, 2 * mRadius , 2 * mRadius)
        mInnerBounds.set(2*dpToPx,2*dpToPx, 2 * mRadius - 2*dpToPx , 2 * mRadius - 2*dpToPx)
        clipPath.addRoundRect(RectF(0f, 0f, 2 * mRadius, 2 * mRadius), 4 * dpToPx, 4 * dpToPx, Path.Direction.CW)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.e("CountDownView", "draw $drawUpto, $mRadius ang ${drawUpto / maxValue * 360}")
        canvas.clipPath(clipPath);
        canvas.drawRect(mBounds, backgroundPaint)
        canvas.drawArc(mArcBounds, startingAngle.toFloat(), drawUpto / maxValue * 360, true, progressBarPaint)
        canvas.drawRoundRect(mInnerBounds, 4 * dpToPx, 4 * dpToPx, progressBarPaint)
//        canvas.drawCircle(mRadius, mRadius, mouthInset * 2, backgroundPaint)
//        mArcBounds[-8f, -8f, mRadius * 2 + 32] = mRadius * 2

//        canvas.drawArc(mArcBounds, 0f, 360f, false, progressBarBackgroundPaint)
    }

}