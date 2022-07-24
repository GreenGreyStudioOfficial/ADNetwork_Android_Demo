package com.mobileadvsdk.presentation

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

internal class MraidController(val activity: WebviewActivity) {


    @JavascriptInterface
    fun getVersion(): String {
        return "3.0"
    }

    @JavascriptInterface
    fun sendMessageToSDK(json: String) {
        Log.e("MraidController sendMessageToSDK", json)
        //TODO something work this
    }

    @JavascriptInterface
    fun broadcastEvent(event: String, data: String) {
        Log.e("MraidController", "event $event, data $data")
        when (toMraidEvent(event)) {
            MRAID_EVENT.ERROR -> {/*TODO()*/
            }
            MRAID_EVENT.READY -> {/*TODO()*/
            }
            MRAID_EVENT.SIZECHANGE -> {/*TODO()*/
            }
            MRAID_EVENT.STATECHANGE -> {/*TODO()*/
            }
            MRAID_EVENT.EXPOSURECHANGE -> {/*TODO()*/
            }
            MRAID_EVENT.AUDIOVOLUMECHANGE -> {/*TODO()*/
            }
            MRAID_EVENT.VIEWABLECHANGE -> {/*TODO()*/
            }
            MRAID_EVENT.INFO -> {/*TODO()*/
            }
        }
    }
}

private enum class MRAID_EVENT {
    ERROR,
    READY,
    SIZECHANGE,
    STATECHANGE,
    EXPOSURECHANGE,
    AUDIOVOLUMECHANGE,
    VIEWABLECHANGE,
    INFO
}

private sealed class JsSdkEvent {
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

private fun toMraidEvent(event: String): MRAID_EVENT = when (event) {
    "error" -> MRAID_EVENT.ERROR
    "ready" -> MRAID_EVENT.READY
    "sizeChange" -> MRAID_EVENT.SIZECHANGE
    "stateChange" -> MRAID_EVENT.STATECHANGE
    "exposureChange" -> MRAID_EVENT.EXPOSURECHANGE
    "audioVolumeChange" -> MRAID_EVENT.AUDIOVOLUMECHANGE
    "viewableChange" -> MRAID_EVENT.VIEWABLECHANGE
    "info" -> MRAID_EVENT.INFO
    else -> MRAID_EVENT.ERROR
}

private fun toSdkEvent(event: String): JsSdkEvent {
    val obj = JSONObject(event)

    return when (obj.getString("m_eventType")) {
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
}
