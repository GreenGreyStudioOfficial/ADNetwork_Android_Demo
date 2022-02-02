package com.mobileadvsdk.presentation.player.model

class VideoClicks {

    var clickThrough: String? = null
    var clickTracking: MutableList<String> = mutableListOf()
    var customClick: MutableList<String> = mutableListOf()

    fun retrieveClickTracking(): MutableList<String> = clickTracking

    override fun toString(): String {
        return "VideoClicks [clickThrough=" + clickThrough + ", clickTracking=[" + listToString(clickTracking) + "], customClick=[" + listToString(
            customClick
        ) + "] ]"
    }

    private fun listToString(list: List<String>?): String =
        list?.let {
            val sb = StringBuffer()
            for (x in it.indices) {
                sb.append(list[x])
            }
            sb.toString()
        } ?: ""
}