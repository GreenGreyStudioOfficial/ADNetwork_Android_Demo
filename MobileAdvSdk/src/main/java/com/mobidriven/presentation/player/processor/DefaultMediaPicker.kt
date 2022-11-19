package com.mobidriven.presentation.player.processor

import android.content.Context
import android.text.TextUtils
import com.mobidriven.presentation.player.model.VASTMediaFile
import java.util.*
import kotlin.math.abs

private const val SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4|3gpp|mp2t|webm|matroska)"

internal class DefaultMediaPicker(private val context: Context) : VASTMediaPicker {

    private var deviceWidth = 0
    private var deviceHeight = 0
    private var deviceArea = 0

    init {
        setDeviceWidthHeight()
    }

    override fun pickVideo(list: List<VASTMediaFile?>?): VASTMediaFile? =
        list?.let {
            if (prefilterMediaFiles(list) == 0) {
                return null
            }
            Collections.sort(it, AreaComparator() as Comparator<in VASTMediaFile?>)
            return getBestMatch(it)
        }

    private fun prefilterMediaFiles(mediaFiles: List<VASTMediaFile?>): Int =
        mediaFiles.filter { !TextUtils.isEmpty(it?.type) && !TextUtils.isEmpty(it?.value) }.size

    private fun setDeviceWidthHeight() {
        val metrics = context.resources.displayMetrics
        deviceWidth = metrics.widthPixels
        deviceHeight = metrics.heightPixels
        deviceArea = deviceWidth * deviceHeight
    }

    private inner class AreaComparator : Comparator<VASTMediaFile> {
        override fun compare(obj1: VASTMediaFile, obj2: VASTMediaFile): Int {
            val obj1Area =
                obj1.width?.let { width -> obj1.height?.let { height -> width.toInt() * height.toInt() } } ?: 0

            val obj2Area =
                obj2.width?.let { width -> obj2.height?.let { height -> width.toInt() * height.toInt() } } ?: 0

            val obj1Diff = abs(obj1Area - deviceArea)
            val obj2Diff = abs(obj2Area - deviceArea)

            return when {
                obj1Diff < obj2Diff -> -1
                obj1Diff > obj2Diff -> 1
                else -> 0
            }
        }
    }

    private fun isMediaFileCompatible(media: VASTMediaFile?): Boolean =
        media?.type?.matches(SUPPORTED_VIDEO_TYPE_REGEX.toRegex()) ?: false

    private fun getBestMatch(list: List<VASTMediaFile?>): VASTMediaFile? =
        list.find { isMediaFileCompatible(it) }
}