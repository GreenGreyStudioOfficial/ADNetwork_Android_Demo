package com.mobidriven

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.mobidriven.datasource.domain.model.AdvertiseType
import com.mobidriven.datasource.domain.model.InitializationErrorType
import com.mobidriven.datasource.domain.model.LoadErrorType
import com.mobidriven.datasource.domain.model.ShowErrorType
import com.mobidriven.presentation.AdvActivity
import com.mobidriven.presentation.AdvProviderImpl
import com.mobidriven.presentation.MraidActivity
import com.mobidriven.presentation.ShowAdv
import com.mobidriven.presentation.WebActivity
import com.mobidriven.presentation.banner.AdvBannerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


@SuppressLint("StaticFieldLeak")
@Keep
object AdvSDK : Application.ActivityLifecycleCallbacks {

    internal var provider: AdvProviderImpl? = null
    internal lateinit var application: Application
    internal var currentActivity: Activity? = null
    internal val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isShowBanner : Boolean = false

    @Keep
    fun initialize(
        activity: Activity,
        gameId: String,
        isTestMode: Boolean,
        listener: IAdInitializationListener
    ) {
        if (provider == null) {
            if (gameId.isEmpty()) {
                listener.onInitializationError(InitializationErrorType.GAME_ID_IS_NULL_OR_EMPTY, "")
                return
            }
            currentActivity = activity
            application = activity.application
            provider = AdvProviderImpl(gameId = gameId, isTestMode = isTestMode, scope = scope)
            listener.onInitializationComplete()
            application.registerActivityLifecycleCallbacks(this)
        } else {
            listener.onInitializationError(InitializationErrorType.SDK_ALREADY_INITIALIZED, "")
        }
        scope.launch {
            provider?.advShowFlow?.collect{
//                Log.e("COLLECT", "$it")
                when(it){
                    is ShowAdv.BannerAdv -> _showBanner(it.id)
                    is ShowAdv.MraidFullScreenAdv -> _showMraid()
                    is ShowAdv.VideoAdv -> _showVideo()
                    is ShowAdv.HideBannerAdv -> _hideBanner(it.id)
                    null -> {/*NOTHING*/}
                    is ShowAdv.WebFullScreenAdv -> _showWeb()
                }
            }
        }
    }

    @Keep
    fun load(advertiseType: AdvertiseType, listener: IAdLoadListener) =
        provider?.loadAdv(advertiseType, listener) ?: listener.onLoadError(LoadErrorType.NOT_INITIALIZED_ERROR)

    @Keep
    fun show(id: String? = null, listener: IAdShowListener) =
        provider?.showAdv(id, listener) ?: listener.onShowError(error = ShowErrorType.NOT_INITIALIZED_ERROR, id = id)

    @Keep
    fun showBanner(id: String? = null, listener: IAdShowBannerListener) =
        provider?.showBanner(id, listener) ?: listener.onBannerShowError(
            ShowErrorType.NOT_INITIALIZED_ERROR,
            id = id
        )

    @Keep
    fun hideBanner(id: String? = null, listener: IAdHideBannerListener) =
        provider?.hideBanner(id, listener) ?: listener.onBannerHideError(
            ShowErrorType.NOT_INITIALIZED_ERROR,
            id = id
        )


    @SuppressLint("StaticFieldLeak")
     private fun _showBanner(id:String?){
//        Log.e("BANNER", "SHOW $id")
        currentActivity ?: return
        val rootView : View? = currentActivity?.findViewById<ViewGroup>(android.R.id.content)?.rootView ?: currentActivity?.window?.decorView?.findViewById(android.R.id.content)
        rootView ?: return
        isShowBanner = true
        provider?.onBannerShow(id)

        rootView.post {
            val layout = AdvBannerView(currentActivity!!)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER)

            if (currentActivity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT){
                val height = rootView.findViewById<View>(android.R.id.navigationBarBackground)?.height
                params.bottomMargin = height ?: 0
            }

            (rootView as ViewGroup).addView(layout, params)
        }
    }

    private fun _hideBanner(id:String?) {
        isShowBanner = false
        if(currentActivity == null){
            provider?.onBannerHideError(id, ShowErrorType.ACTIVITY_WAS_DESTROYED)
            return
        }
        val rootView : View? = currentActivity?.findViewById<ViewGroup>(android.R.id.content)?.rootView ?: currentActivity?.window?.decorView?.findViewById(android.R.id.content)
        val bannerView = rootView?.findViewById<View>(R.id.adv_banner_view)

        if(rootView == null || bannerView == null){
            provider?.onBannerHideError(id, ShowErrorType.BANNER_VIEW_NOT_FOUND)
            return
        }

        (rootView as ViewGroup).removeView(bannerView)
        provider?.onBannerHide(id)
    }

    private fun _showMraid(){
        application.startActivity(
            Intent(
                application,
                MraidActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun _showWeb(){
        application.startActivity(
            Intent(
                application,
                WebActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun _showVideo(){
        application.startActivity(
            Intent(
                application,
                AdvActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        currentActivity = activity
        val rootView  = activity.findViewById<ViewGroup>(android.R.id.content).rootView ?: activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        if (isShowBanner) {
            val banner = rootView.findViewById<View>(R.id.adv_banner_view)
            if(banner==null) _showBanner(provider?.advId)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    internal fun createAdHtml(original: String): String {
        val meta = "<meta name='viewport' content='width=device-width'/>"
        val centerAdStyle =
            "<style type='text/css'>html,body {margin: 0;padding: 0;width: 100%;height: 100%;}html {display: table;}body {display: table-cell;vertical-align: middle;text-align: center;}</style>"

        val clearHtml = original
            .replace("\\\\n".toRegex(), "")
            .replace("\\\\{1,3}\"".toRegex(),"\"")


        return if(clearHtml.startsWith("<html>")) clearHtml else ("<html><head>" + meta + "</head>"
                + "<body>"
                + centerAdStyle
                + clearHtml
                ) + "</body></html>"
    }


}