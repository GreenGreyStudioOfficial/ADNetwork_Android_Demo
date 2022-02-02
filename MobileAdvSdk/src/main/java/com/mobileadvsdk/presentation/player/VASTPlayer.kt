package com.mobileadvsdk.presentation.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.mobileadvsdk.R
import com.mobileadvsdk.datasource.domain.model.AdvertiseType
import com.mobileadvsdk.presentation.player.model.TrackingEventsType
import com.mobileadvsdk.presentation.player.model.VASTModel
import com.mobileadvsdk.presentation.player.processor.CacheFileManager
import com.mobileadvsdk.presentation.player.util.HttpTools.httpGetURL
import com.mobileadvsdk.presentation.player.util.VASTLog.d
import com.mobileadvsdk.presentation.player.util.VASTLog.e
import com.mobileadvsdk.presentation.player.util.VASTLog.i
import com.mobileadvsdk.presentation.player.util.VASTLog.v
import com.mobileadvsdk.presentation.player.util.VASTLog.w
import java.util.*
import kotlin.math.abs

class VASTPlayer : RelativeLayout, View.OnClickListener {
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private var playerView: PlayerView? = null
    private var progressBar: ProgressBar? = null
    private var type = AdvertiseType.INTERSTITIAL

    fun setType(type: AdvertiseType) {
        this.type = type
    }

    /**
     * Player type will lead to different layouts and behaviour to improve campaign type
     */
    enum class CampaignType {
        // Cost per click, this will improve player click possibilities
        CPC,  // Cost per million (of impressions), this will improve impression behaviour (keep playing)
        CPM
    }

    /**
     * Callbacks for following the player behaviour
     */
    interface Listener {
        fun onVASTPlayerLoadFinish()
        fun onVASTPlayerFail(exception: Exception?)
        fun onVASTPlayerCacheNotFound()
        fun onVASTPlayerPlaybackStart()
        fun onVASTPlayerPlaybackFinish()
        fun onVASTPlayerOpenOffer()
        fun onVASTPlayerOnFirstQuartile()
        fun onVASTPlayerOnMidpoint()
        fun onVASTPlayerOnThirdQuartile()
        fun onVASTPlayerClose(needToConfirm: Boolean)
    }

    private enum class PlayerState {
        Empty, Loading, Ready, Playing, Pause
    }

    // LISTENERS
    private var mListener: Listener? = null

    // TIMERS
    private var mLayoutTimer: Timer? = null
    private var mProgressTimer: Timer? = null
    private var mTrackingEventsTimer: CountDownTimer? = null

    // TRACKING
    private var mTrackingEventMap: HashMap<TrackingEventsType, MutableList<String>>? = null

    // DATA
    private var mVastModel: VASTModel? = null
    private var mSkipName: String? = null
    private var mSkipDelay = 0

    // PLAYER
    private var simpleExoPlayer: SimpleExoPlayer? = null

    // VIEWS
    private var mRoot: View? = null
    private var mOpen: View? = null

    // Player
    private var mSkip: TextView? = null
    private var mMute: AppCompatImageView? = null
    private var mCountDown: CountDownView? = null

    // OTHERS
    private var mMainHandler: Handler? = null
    private var mVideoHeight = 0
    private var mVideoWidth = 0
    private var mIsVideoMute = false
    private var mIsBufferingShown = false
    private var mIsDataSourceSet = false
    private var mCampaignType = CampaignType.CPM
    private var mPlayerState = PlayerState.Empty
    private var mProgressTracker: MutableList<Int> = mutableListOf()
    private var mTargetAspect = -1.0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Sets the desired aspect ratio.  The value is `width / height`.
     */
    fun setAspectRatio(aspectRatio: Double) {
        require(aspectRatio >= 0)
        Log.d(TAG, "Setting aspect ratio to $aspectRatio (was $mTargetAspect)")
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Target aspect ratio will be < 0 if it hasn't been set yet.  In that case,
        // we just use whatever we've been handed.
        var widthMeasure = widthMeasureSpec
        var heightMeasure = heightMeasureSpec
        if (mTargetAspect > 0) {
            var initialWidth = MeasureSpec.getSize(widthMeasure)
            var initialHeight = MeasureSpec.getSize(heightMeasure)

            // factor the padding out
            val horizPadding = paddingLeft + paddingRight
            val vertPadding = paddingTop + paddingBottom
            initialWidth -= horizPadding
            initialHeight -= vertPadding
            val viewAspectRatio = initialWidth.toDouble() / initialHeight
            val aspectDiff = mTargetAspect / viewAspectRatio - 1
            if (abs(aspectDiff) < 0.01) {
                // We're very close already.  We don't want to risk switching from e.g. non-scaled
                // 1280x720 to scaled 1280x719 because of some floating-point round-off error,
                // so if we're really close just leave it alone.
                Log.v(
                    TAG, "aspect ratio is good (target=" + mTargetAspect +
                            ", view=" + initialWidth + "x" + initialHeight + ")"
                )
            } else {
                if (aspectDiff > 0) {
                    // limited by narrow width; restrict height
                    initialHeight = (initialWidth / mTargetAspect).toInt()
                } else {
                    // limited by short height; restrict width
                    initialWidth = (initialHeight * mTargetAspect).toInt()
                }
                Log.v(
                    TAG, "new size=" + initialWidth + "x" + initialHeight + " + padding " +
                            horizPadding + "x" + vertPadding
                )
                initialWidth += horizPadding
                initialHeight += vertPadding
                widthMeasure = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY)
                heightMeasure = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY)
            }
        }
        super.onMeasure(widthMeasure, heightMeasure)
    }

    //=======================================================
    // State machine
    //=======================================================
    private fun canSetState(playerState: PlayerState): Boolean =
        when (playerState) {
            PlayerState.Empty, PlayerState.Pause, PlayerState.Loading -> true
            PlayerState.Ready -> PlayerState.Loading == mPlayerState
            PlayerState.Playing -> mPlayerState == PlayerState.Ready
                    || mPlayerState == PlayerState.Pause
        }

    /**
     * this method controls the associated state machine of the video player behaviour
     *
     * @param playerState state to set
     * @return
     */
    private fun setState(playerState: PlayerState) {
        Log.v(TAG, "setState: " + playerState.name)
        if (canSetState(playerState)) {
            when (playerState) {
                PlayerState.Empty -> setEmptyState()
                PlayerState.Loading -> setLoadingState()
                PlayerState.Ready -> setReadyState()
                PlayerState.Playing -> setPlayingState()
                PlayerState.Pause -> setPauseState()
            }
            mPlayerState = playerState
        }
    }

    private fun setEmptyState() {
        Log.v(TAG, "setEmptyState")

        // Visual aspect total empty
        hidePlayerLayout()
        hideOpen()
        /**
         * Do not change this order, since cleaning the media player before invalidating timers
         * could make the timers threads access an invalid media player
         */
        stopTimers()
        cleanMediaPlayer()
        // Reset all other items
        mIsDataSourceSet = false
        mVastModel = null
        mTrackingEventMap = null
        mProgressTracker = mutableListOf()
    }

    private fun setLoadingState() {
        Log.v(TAG, "setLoadingState")

        // Show loader
        hidePlayerLayout()
        mVastModel?.trackingUrls?.let {
            mTrackingEventMap = it
        }
        createMediaPlayer()
        turnVolumeOff()
        startCaching()
    }

    private fun setReadyState() {
        Log.v(TAG, "setReadyState")
        hidePlayerLayout()
        showOpen()
        turnVolumeOff()
    }

    private fun setPlayingState() {
        Log.v(TAG, "setPlayingState")
        showOpen()
        showPlayerLayout()
        // calculateAspectRatio();
        refreshVolume()
        simpleExoPlayer?.play()
        startTimers()
    }

    private fun setPauseState() {
        Log.v(TAG, "setPauseState")
        hidePlayerLayout()
        showOpen()
        turnVolumeOff()
        refreshVolume()
    }
    //=======================================================
    // PUBLIC
    //=======================================================
    /**
     * Constructor, generally used automatically by a layout inflater
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mMainHandler = Handler(getContext().mainLooper)
        createMediaPlayer()
        createLayout()
        setEmptyState()
    }

    /**
     * Sets listener for callbacks related to status of player
     *
     * @param listener Listener
     */
    fun setListener(listener: Listener?) {
        Log.v(TAG, "setListener")
        mListener = listener
    }

    /**
     * Sets the campaign type of the player
     *
     * @param campaignType campign type
     */
    fun setCampaignType(campaignType: CampaignType) {
        Log.v(TAG, "setCampaignType")
        mCampaignType = campaignType
    }

    /**
     * This will set up the skip button behaviour setting a name and a delay for it to show up
     *
     * @param delay delay in milliseconds to show the skip button, negative values will disable
     * the button
     */
    private fun setSkip(delay: Int) {
        if (delay < 0) {
            Log.w(TAG, "Skip time set to negative value, this will disable the button")
        }
        mSkipName = "Close"
        mSkipDelay = delay
    }
    //=======================================================
    // Player actions
    //=======================================================
    /**
     * Starts loading a video VASTModel in the player, it will notify when it's ready with
     * CachingListener.onVASTPlayerCachingFinish(), so you can start video reproduction.
     *
     * @param model model containing the parsed VAST XML
     */
    fun load(model: VASTModel?) {
        v(TAG, "load")
        // Clean, assign, load
        setState(PlayerState.Empty)
        mVastModel = model
        mIsDataSourceSet = false
        setState(PlayerState.Loading)
    }

    /**
     * Starts video playback if possible
     */
    fun play() {
        v(TAG, "play")
        when {
            canSetState(PlayerState.Playing) -> {
                setState(PlayerState.Playing)
            }
            mPlayerState == PlayerState.Empty -> {
                setState(PlayerState.Ready)
            }
            else -> {
                e(TAG, "ERROR, player in wrong state: " + mPlayerState.name)
            }
        }
    }

    /**
     * Stops video playback
     */
    fun stop() {
        v(TAG, "stop")
        if (canSetState(PlayerState.Loading) && mIsDataSourceSet) {
            stopTimers()
            simpleExoPlayer?.stop()
            simpleExoPlayer?.release()
            mIsDataSourceSet = false
            quartileSet.clear()
            setState(PlayerState.Loading)
        } else {
            e(TAG, "ERROR, player in wrong state: " + mPlayerState.name)
        }
    }

    /**
     * Stops video playback
     */
    fun pause() {
        v(TAG, "pause")
        if (canSetState(PlayerState.Pause) && mIsDataSourceSet) {
            if (simpleExoPlayer != null && simpleExoPlayer?.isPlaying == true) {
                stopQuartileTimer()
                simpleExoPlayer?.pause()
            }
            setState(PlayerState.Pause)
        } else {
            e(TAG, "ERROR, player in wrong state: " + mPlayerState.name)
        }
    }

    /**
     * Destroys current player and clears all loaded data and tracking items
     */
    fun destroy() {
        v(TAG, "clear")
        setState(PlayerState.Empty)
    }

    //=======================================================
    // Private
    //=======================================================
    // User Interaction
    //-------------------------------------------------------
    private fun onMuteClick() {
        v(TAG, "onMuteClick")
        if (simpleExoPlayer != null) {
            processEvent(if (mIsVideoMute) TrackingEventsType.unmute else TrackingEventsType.mute)
            mIsVideoMute = !mIsVideoMute
            refreshVolume()
        }
    }

    fun onSkipClick() {
        v(TAG, "onSkipClick")
        simpleExoPlayer?.let {
            mListener?.onVASTPlayerClose(needShowDialogClose)
            pause()
        } ?: run {
            mListener?.onVASTPlayerClose(needShowDialogClose)
        }
    }

    fun onSkipConfirm() {
        processEvent(TrackingEventsType.close)
        destroy()
    }

    fun onOpenClick() {
        v(TAG, "onOpenClick")
        openOffer()
        pause()
    }

    private fun openOffer() {
        val clickThroughUrl = mVastModel?.videoClicks?.clickThrough
        d(TAG, "openOffer - clickThrough url: $clickThroughUrl")
        // Before we send the app to the click through url, we will process ClickTracking URL's.
        val urls = mVastModel?.videoClicks?.retrieveClickTracking()
        fireUrls(urls)
        // Navigate to the click through url
        clickThroughUrl?.let {
            val uri = Uri.parse(clickThroughUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            invokeOnPlayerOpenOffer()
        }
    }

    // Layout
    //-------------------------------------------------------
    private fun createLayout() {
        v(TAG, "createLayout")
        if (mRoot == null) {
            mRoot = LayoutInflater.from(context).inflate(R.layout.pubnative_player, null)
            playerView = mRoot?.findViewById<View>(R.id.playerView) as PlayerView
            progressBar = mRoot?.findViewById<View>(R.id.progress) as ProgressBar
            mMute = mRoot?.findViewById<View>(R.id.mute) as AppCompatImageView
            mMute?.visibility = INVISIBLE
            mMute?.setOnClickListener(this)
            mCountDown = mRoot?.findViewById<View>(R.id.count_down) as CountDownView
            mCountDown?.visibility = INVISIBLE
            mSkip = mRoot?.findViewById<View>(R.id.skip) as TextView
            mSkip?.visibility = INVISIBLE
            mSkip?.setOnClickListener(this)
            mOpen = mRoot?.findViewById(R.id.open)
            mOpen?.visibility = INVISIBLE
            mOpen?.setOnClickListener(this)
            addView(
                mRoot, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun hideOpen() {
        mOpen?.visibility = INVISIBLE
    }

    private fun showOpen() {
        mOpen?.visibility = VISIBLE
    }

    private fun hidePlayerLayout() {
        mSkip?.visibility = INVISIBLE
        mMute?.visibility = INVISIBLE
        mCountDown?.visibility = INVISIBLE
    }

    private fun showPlayerLayout() {
        mMute?.visibility = VISIBLE
        mCountDown?.visibility = VISIBLE
    }

    // Media player
    //-------------------------------------------------------
    private fun createMediaPlayer() {
        v(TAG, "createMediaPlayer")
        if (simpleExoPlayer == null) {
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(CacheFileManager.getSimpleCache())
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            simpleExoPlayer = SimpleExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory)).build()
            simpleExoPlayer?.addAnalyticsListener(object : AnalyticsListener {
                override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
                    if (state == Player.STATE_ENDED) {
                        v(TAG, "onCompletion -- (MediaPlayer callback)")
                        processEvent(TrackingEventsType.complete)
                        invokeOnPlayerPlaybackFinish()
                        mCountDown?.visibility = INVISIBLE
                    } else if (state == Player.STATE_READY) {
                        v(TAG, "onPrepared --(MediaPlayer callback) ....about to play")
                        setState(PlayerState.Ready)
                        invokeOnPlayerLoadFinish()
                    }
                }

                override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: ExoPlaybackException) {
                    v(TAG, "onError -- (MediaPlayer callback)")
                    processErrorEvent()
                    mListener?.onVASTPlayerCacheNotFound()
                    invokeOnFail(Exception("VASTPlayer error: " + error.message))
                    destroy()
                }

                override fun onVideoSizeChanged(
                    eventTime: AnalyticsListener.EventTime,
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    v(TAG, "onVideoSizeChanged -- $width x $height")
                    mVideoWidth = width
                    mVideoHeight = height
                }
            })
        }
    }

    private fun cleanMediaPlayer() {
        v(TAG, "cleanUpMediaPlayer")
        if (simpleExoPlayer != null) {
            turnVolumeOff()
            simpleExoPlayer?.stop()
            simpleExoPlayer?.release()
            simpleExoPlayer?.clearVideoSurface()
            simpleExoPlayer = null
        }
    }

    fun refreshVolume() {
        if (mIsVideoMute) {
            turnVolumeOff()
            mMute?.setImageResource(R.drawable.ic_baseline_volume_off_24)
        } else {
            turnVolumeOn()
            mMute?.setImageResource(R.drawable.ic_baseline_volume_up_24)
        }
    }

    fun turnVolumeOff() {
        simpleExoPlayer?.volume = 0.0f
    }

    fun turnVolumeOn() {
        simpleExoPlayer?.volume = 1.0f
    }

    protected fun calculateAspectRatio() {
        v(TAG, "calculateAspectRatio")
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            w(TAG, "calculateAspectRatio - video source width or height is 0, skipping...")
            return
        }
        val widthRatio = 1.0 * width / mVideoWidth
        val heightRatio = 1.0 * height / mVideoHeight
        val scale = Math.max(widthRatio, heightRatio)
        i(TAG, " view size:     " + width + "x" + height)
        i(TAG, " video size:    " + mVideoWidth + "x" + mVideoHeight)
        i(TAG, " surface size:  " + mVideoWidth + "x" + mVideoHeight)
        updateLayout()
        setAspectRatio(mVideoWidth.toDouble() / mVideoHeight)
    }

    private fun updateLayout() {
        v(TAG, "updateLayout")
        val muteParams = mMute?.layoutParams as LayoutParams
        muteParams.addRule(ALIGN_TOP, R.id.playerView)
        muteParams.addRule(ALIGN_LEFT, R.id.playerView)
        mMute?.layoutParams = muteParams
        val openParams = mOpen?.layoutParams as LayoutParams
        openParams.addRule(ALIGN_TOP, R.id.playerView)
        openParams.addRule(ALIGN_RIGHT, R.id.playerView)
        mOpen?.layoutParams = openParams
        val countDownParams = mCountDown?.layoutParams as LayoutParams
        countDownParams.addRule(ALIGN_BOTTOM, R.id.playerView)
        countDownParams.addRule(ALIGN_LEFT, R.id.playerView)
        mCountDown?.layoutParams = countDownParams
        val skipParams = mSkip?.layoutParams as LayoutParams
        skipParams.addRule(ALIGN_BOTTOM, R.id.playerView)
        skipParams.addRule(ALIGN_RIGHT, R.id.playerView)
        mSkip?.layoutParams = skipParams
    }

    private fun startCaching() {
        v(TAG, "startCaching")
        try {
            if (!mIsDataSourceSet) {
                mIsDataSourceSet = true
                val videoURL = mVastModel?.pickedMediaFileURL ?: ""
                val mediaItem = MediaItem.fromUri(videoURL)
                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
                playerView?.player = simpleExoPlayer
                simpleExoPlayer?.seekTo(0, 0)
                simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
                simpleExoPlayer?.setMediaSource(mediaSource, true)
                simpleExoPlayer?.prepare()
            }
            simpleExoPlayer?.prepare()
        } catch (exception: Exception) {
            invokeOnFail(exception)
            destroy()
        }
    }

    // Event processing
    //-------------------------------------------------------
    private fun processEvent(eventName: TrackingEventsType) {
        v(TAG, "processEvent: $eventName")
        mTrackingEventMap?.let {
            val urls = it[eventName]
            fireUrls(urls)
        }
    }

    private fun processImpressions() {
        v(TAG, "processImpressions")
        val impressions = mVastModel?.impressions
        fireUrls(impressions)
    }

    private fun processErrorEvent() {
        v(TAG, "processErrorEvent")
        val errorUrls = mVastModel?.errorUrl
        fireUrls(errorUrls)
    }

    private fun fireUrls(urls: List<String>?) {
        v(TAG, "fireUrls")
        if (urls != null) {
            for (url in urls) {
                v(TAG, "\tfiring url:$url")
                httpGetURL(url)
            }
        } else {
            d(TAG, "\turl list is null")
        }
    }

    //=======================================================
    // Timers
    //=======================================================
    private fun stopTimers() {
        v(TAG, "stopTimers")
        stopQuartileTimer()
        stopLayoutTimer()
        stopVideoProgressTimer()
        mMainHandler?.removeMessages(0)
    }

    private fun startTimers() {
        setSkip(mVastModel?.skipOffset ?: 1)
        v(TAG, "startTimers")

        // Stop previous timers so they don't remain hold
        stopTimers()

        // start timers
        startQuartileTimer()
        startLayoutTimer()
        startVideoProgressTimer()
    }

    // Progress timer
    //-------------------------------------------------------
    private fun startVideoProgressTimer() {
        d(TAG, "startVideoProgressTimer")
        mProgressTimer = Timer()
        mProgressTracker = ArrayList()
        mProgressTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (mProgressTracker.size > MAX_PROGRESS_TRACKING_POINTS) {
                    val firstPosition = mProgressTracker[0]
                    val lastPosition = mProgressTracker[mProgressTracker.size - 1]
                    if (lastPosition > firstPosition) {
                        if (mIsBufferingShown) {
                            mIsBufferingShown = false
                        }
                    } else {
                        if (!mIsBufferingShown) {
                            mIsBufferingShown = true
                        }
                    }
                    mProgressTracker.removeAt(0)
                }
                mMainHandler?.post {
                    try {
                        mProgressTracker.add(simpleExoPlayer?.currentPosition?.toInt() ?: 1)
                    } catch (ignored: Exception) {
                    }
                }
            }
        }, 0, TIMER_PROGRESS_INTERVAL)
    }

    private fun stopVideoProgressTimer() {
        d(TAG, "stopVideoProgressTimer")
        if (mProgressTimer != null) {
            mProgressTimer?.cancel()
            mProgressTimer = null
        }
    }

    private val quartileSet: MutableSet<String> = HashSet()

    // Quartile timer
    //-------------------------------------------------------
    private fun startQuartileTimer() {
        progressBar?.visibility = VISIBLE
        v(TAG, "startQuartileTimer")
        val duration = simpleExoPlayer?.duration ?: 1
        mTrackingEventsTimer = object : CountDownTimer(duration, 250) {
            override fun onTick(millisUntilFinished: Long) {
                val currentPosition = simpleExoPlayer?.currentPosition ?: 0L
                if (currentPosition == 0L) {
                    return
                }

                val percentage = (currentPosition * 100 / duration).toInt()
                progressBar?.progress = percentage

                if (percentage < 25 && !quartileSet.contains(TrackingEventsType.start.toString())) {
                    i(TAG, "Video at start: ($percentage%)")
                    processImpressions()
                    processEvent(TrackingEventsType.start)
                    quartileSet.add(TrackingEventsType.start.toString())
                    invokeOnPlayerPlaybackStart()
                } else if (percentage in 25..49 && !quartileSet.contains(TrackingEventsType.firstQuartile.toString())) {
                    i(TAG, "Video at first quartile: ($percentage%)")
                    processEvent(TrackingEventsType.firstQuartile)
                    quartileSet.add(TrackingEventsType.firstQuartile.toString())
                    mListener?.onVASTPlayerOnFirstQuartile()
                } else if (percentage in 50..74 && !quartileSet.contains(TrackingEventsType.midpoint.toString())) {
                    i(TAG, "Video at midpoint: ($percentage%)")
                    processEvent(TrackingEventsType.midpoint)
                    quartileSet.add(TrackingEventsType.midpoint.toString())
                    mListener?.onVASTPlayerOnMidpoint()
                } else if (percentage in 75..99 && !quartileSet.contains(TrackingEventsType.thirdQuartile.toString())) {
                    i(TAG, "Video at third quartile: ($percentage%)")
                    processEvent(TrackingEventsType.thirdQuartile)
                    quartileSet.add(TrackingEventsType.thirdQuartile.toString())
                    mListener?.onVASTPlayerOnThirdQuartile()
                }
            }

            override fun onFinish() {
                progressBar?.visibility = GONE
            }
        }.start()
    }

    private fun stopQuartileTimer() {
        v(TAG, "stopQuartileTimer")
        if (mTrackingEventsTimer != null) {
            mTrackingEventsTimer?.cancel()
            mTrackingEventsTimer = null
        }
    }

    private var needShowDialogClose = false

    // Layout timer
    //-------------------------------------------------------
    private fun startLayoutTimer() {
        v(TAG, "startLayoutTimer")
        mLayoutTimer = Timer()
        mLayoutTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (simpleExoPlayer == null) {
                    cancel()
                    return
                }
                // Execute with handler to be sure we execute this on the UIThread
                mMainHandler?.post {
                    try {
                        if (simpleExoPlayer?.isPlaying == true) {
                            val currentPositionElvis = simpleExoPlayer?.currentPosition?.toInt() ?: 1
                            val currentPosition = currentPositionElvis / 1000
                            if (type === AdvertiseType.REWARDED && mVastModel?.skipOffset != -1) {
                                mCountDown?.setProgress(currentPosition, mVastModel?.skipOffset ?: 0)
                                mSkip?.text = mSkipName
                                mSkip?.visibility = VISIBLE
                                needShowDialogClose = currentPosition < mSkipDelay
                            } else if (type === AdvertiseType.REWARDED && mVastModel?.skipOffset == -1) {
                                mCountDown?.setProgress(currentPosition, simpleExoPlayer?.duration?.toInt() ?: 1)
                                needShowDialogClose = true
                            } else if (type === AdvertiseType.INTERSTITIAL && mVastModel?.skipOffset != -1) {
                                mCountDown?.setProgress(currentPosition, mVastModel?.skipOffset ?: 1)
                                if (currentPosition > mSkipDelay) {
                                    mSkip?.text = mSkipName
                                    mSkip?.visibility = VISIBLE
                                }
                                needShowDialogClose = false
                            } else if (type === AdvertiseType.INTERSTITIAL && mVastModel?.skipOffset == -1) {
                                mCountDown?.setProgress(currentPosition, simpleExoPlayer?.duration?.toInt() ?: 1)
                                mSkip?.text = mSkipName
                                mSkip?.visibility = VISIBLE
                                needShowDialogClose = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Layout timer error: $e")
                        cancel()
                        return@post
                    }
                }
            }
        }, 0, TIMER_LAYOUT_INTERVAL)
    }

    private fun stopLayoutTimer() {
        d(TAG, "stopLayoutTimer")
        mLayoutTimer?.cancel()
        mLayoutTimer = null
    }

    // Listener helpers
    //-------------------------------------------------------
    private fun invokeOnPlayerOpenOffer() {
        v(TAG, "invokeOnPlayerClick")
        if (mListener != null) {
            mListener?.onVASTPlayerOpenOffer()
        }
    }

    private fun invokeOnPlayerLoadFinish() {
        v(TAG, "invokeOnPlayerLoadFinish")
        if (mListener != null) {
            mListener?.onVASTPlayerLoadFinish()
        }
    }

    private fun invokeOnFail(exception: Exception) {
        v(TAG, "invokeOnFail")
        if (mListener != null) {
            mListener?.onVASTPlayerFail(exception)
        }
    }

    private fun invokeOnPlayerPlaybackStart() {
        v(TAG, "invokeOnPlayerPlaybackStart")
        if (mListener != null) {
            mListener?.onVASTPlayerPlaybackStart()
        }
    }

    private fun invokeOnPlayerPlaybackFinish() {
        v(TAG, "invokeOnPlayerPlaybackFinish")
        if (mListener != null) {
            mListener?.onVASTPlayerPlaybackFinish()
        }
    }

    //=============================================
    // CALLBACKS
    //=============================================
    // MediaPlayer.OnCompletionListener
    //---------------------------------------------
    // View.OnClickListener
    //---------------------------------------------
    override fun onClick(view: View) {
        v(TAG, "onClick -- (View.OnClickListener callback)")
        if (mOpen === view) {
            onOpenClick()
        } else if (mSkip === view) {
            onSkipClick()
        } else if (mMute === view) {
            onMuteClick()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.v(TAG, "onSizeChanged")
        super.onSizeChanged(w, h, oldw, oldh)
        Handler().post {
            // calculateAspectRatio();
        }
    }

    companion object {
        private val TAG = VASTPlayer::class.java.name
        private const val TEXT_BUFFERING = "Buffering..."
        private const val TIMER_TRACKING_INTERVAL: Long = 250
        private const val TIMER_PROGRESS_INTERVAL: Long = 50
        private const val TIMER_LAYOUT_INTERVAL: Long = 50
        private const val MAX_PROGRESS_TRACKING_POINTS = 20
    }
}