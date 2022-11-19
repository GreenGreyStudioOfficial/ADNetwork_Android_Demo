package com.mobidriven.presentation

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import org.json.JSONObject

@Keep
internal class MraidController(val onJsSdkEvent: (JsSdkEvent) -> Unit) {

    @JavascriptInterface
    fun sendMessageToSDK(json: String) {
//        Log.e("MraidController sendMessageToSDK", json)
        toSdkEvent(json)?.let {
            Handler(Looper.getMainLooper()).post {
                onJsSdkEvent(it)
            }
        }
    }
}

internal sealed class JsSdkEvent {
    data class Open(val uri: String) : JsSdkEvent()
    object Close : JsSdkEvent()
    object Unload : JsSdkEvent()
    data class Expand(val uri: String) : JsSdkEvent()
    data class PlayVideo(val uri: String) : JsSdkEvent()
    data class Resize(
        val width: Int,
        val height: Int,
        val offsetX: Int,
        val offsetY: Int,
        val customClosePosition: String,
        val allowOffscreen: Boolean
    ) : JsSdkEvent()

    data class StorePicture(val uri: String) : JsSdkEvent()
    object CreateCalendarEvent : JsSdkEvent()
    data class SetOrientationProperties(
        val allowOrientationChange: Boolean,
        val forceOrientation: String,
    ) : JsSdkEvent()

    data class SetExpandProperties(
        val width: Int,
        val height: Int,
        val isModal: Boolean,
    ) : JsSdkEvent()

    data class RewardReceived(val value: Boolean) : JsSdkEvent()
    data class ContentLoaded(val value: Boolean) : JsSdkEvent()
}

private fun toSdkEvent(event: String): JsSdkEvent? {
    return try {
        val obj = JSONObject(event)
        when (obj.getString("m_eventType")) {
            "open" -> JsSdkEvent.Open(uri = obj.getString("m_uri"))
            "close" -> JsSdkEvent.Close
            "unload" -> JsSdkEvent.Unload
            "expand" -> JsSdkEvent.Expand(uri = obj.getString("m_uri"))
            "playVideo" -> JsSdkEvent.PlayVideo(uri = obj.getString("m_uri"))
            "resize" -> JsSdkEvent.Resize(
                width = obj.getInt("m_width"),
                height = obj.getInt("m_height"),
                offsetX = obj.getInt("m_offsetX"),
                offsetY = obj.getInt("m_offsetY"),
                customClosePosition = obj.getString("m_customClosePosition"),
                allowOffscreen = obj.getBoolean("m_allowOffscreen")
            )
            "storePicture" -> JsSdkEvent.StorePicture(uri = obj.getString("m_uri"))
            "createCalendarEvent" -> JsSdkEvent.CreateCalendarEvent
            "setOrientationProperties" -> JsSdkEvent.SetOrientationProperties(
                allowOrientationChange = obj.getBoolean("m_allowOrientationChange"),
                forceOrientation = obj.getString("m_forceOrientation"),
            )
            "setExpandProperties" -> JsSdkEvent.SetExpandProperties(
                width = obj.getInt("m_width"),
                height = obj.getInt("m_width"),
                isModal = obj.getBoolean("m_isModal")
            )
            "rewardReceived" -> JsSdkEvent.RewardReceived(value = obj.getBoolean("m_value"))
            "contentLoaded" -> JsSdkEvent.ContentLoaded(value = obj.getBoolean("m_value"))
            else -> error("not supported event type ${obj.getString("m_eventType")}")
        }
    } catch (e: Exception) {
        Log.d("MRAID Event", "${e.message}")
        null
    }

}
