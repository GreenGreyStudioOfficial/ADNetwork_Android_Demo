package com.mobileadvsdk.presentation.player.processor

import android.content.Context
import android.text.TextUtils
import com.mobileadvsdk.presentation.player.model.VASTMediaFile
import com.mobileadvsdk.presentation.player.util.VASTLog.d
import com.mobileadvsdk.presentation.player.util.VASTLog.v
import java.util.*
import kotlin.math.abs

private const val SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4|3gpp|mp2t|webm|matroska)"
private const val TAG = "DefaultMediaPicker"

internal class DefaultMediaPicker(private val context: Context) : VASTMediaPicker {

    private var deviceWidth = 0
    private var deviceHeight = 0
    private var deviceArea = 0

    init {
        setDeviceWidthHeight()
    }

    override fun pickVideo(list: List<VASTMediaFile?>?): VASTMediaFile? {
        //make sure that the list of media files contains the correct attributes
        if (list == null || prefilterMediaFiles(list) == 0) {
            return null
        }
        Collections.sort(list, AreaComparator())
        return getBestMatch(list)
    }

    private fun prefilterMediaFiles(mediaFiles: List<VASTMediaFile?>): Int {
        val iter = mediaFiles.iterator()
        while (iter.hasNext()) {
            val mediaFile = iter.next()
            // type attribute
            if (TextUtils.isEmpty(mediaFile?.type)) {
                d(TAG, "Validator error: mediaFile type empty")
                iter.remove()
                continue
            }

            // mediaFile url
            if (TextUtils.isEmpty(mediaFile?.value)) {
                d(TAG, "Validator error: mediaFile url empty")
                iter.remove()
            }
        }
        return mediaFiles.size
    }

    private fun setDeviceWidthHeight() {
        val metrics = context.resources.displayMetrics
        deviceWidth = metrics.widthPixels
        deviceHeight = metrics.heightPixels
        deviceArea = deviceWidth * deviceHeight
    }

    private inner class AreaComparator : Comparator<VASTMediaFile> {
        override fun compare(obj1: VASTMediaFile, obj2: VASTMediaFile): Int {

            // get area of the video of the two MediaFiles
            val obj1Area = obj1.width!!.toInt() * obj1.height!!.toInt()
            val obj2Area = obj2.width!!.toInt() * obj2.height!!.toInt()

            // get the difference between the area of the MediaFile and the area of the screen
            val obj1Diff = abs(obj1Area - deviceArea)
            val obj2Diff = abs(obj2Area - deviceArea)
            v(TAG, "AreaComparator: obj1:$obj1Diff obj2:$obj2Diff")

            // choose the MediaFile which has the lower difference in area
            return when {
                obj1Diff < obj2Diff -> {
                    -1
                }
                obj1Diff > obj2Diff -> {
                    1
                }
                else -> {
                    0
                }
            }
        }
    }

    private fun isMediaFileCompatible(media: VASTMediaFile?): Boolean {
        // check if the MediaFile is compatible with the device.
        // further checks can be added here
        val type = media?.type
        return type?.matches(SUPPORTED_VIDEO_TYPE_REGEX.toRegex()) ?: false
    }

    private fun getBestMatch(list: List<VASTMediaFile?>): VASTMediaFile? {
        d(TAG, "getBestMatch")
        // Iterate through the sorted list and return the first compatible media.
        // If none of the media file is compatible, return null
        for (media in list) {
            if (isMediaFileCompatible(media)) {
                return media
            }
        }
        return null
    }
}