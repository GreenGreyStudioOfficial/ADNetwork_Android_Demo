package com.mobileadvsdk.presentation.player.processor

import android.text.TextUtils
import com.mobileadvsdk.presentation.player.model.VASTMediaFile
import com.mobileadvsdk.presentation.player.model.VASTModel
import com.mobileadvsdk.presentation.player.util.VASTLog

internal object VASTModelPostValidator {
    private val TAG = VASTModelPostValidator::class.java.name

    // This method tries to make sure that there is at least 1 Media file to
    // be used for VASTActivity. Also, if the boolean validateModel is true, it will
    // do additional validations which includes "at least 1 impression tracking url's is required'
    // If any of the above fails, it returns false. The false indicates that you can stop proceeding
    // further to display this on the MediaPlayer.
    @JvmStatic
    fun validate(model: VASTModel?, mediaPicker: VASTMediaPicker?): Boolean {
        VASTLog.d(TAG, "validate")
        if (!validateModel(model)) {
            VASTLog.d(TAG, "Validator returns: not valid (invalid model)")
            return false
        }
        var isValid = false

        // Must have a MediaPicker to choose one of the MediaFile element from XML
        if (mediaPicker != null) {
            val mediaFiles: List<VASTMediaFile?>? = model?.mediaFiles
            val mediaFile = mediaPicker.pickVideo(mediaFiles)
            if (mediaFile != null) {
                val url = mediaFile.value
                if (!TextUtils.isEmpty(url)) {
                    isValid = true
                    // Let's set this value inside VASTModel so that it can be
                    // accessed from VASTPlayer
                    model?.pickedMediaFileURL = url ?: ""
                    VASTLog.d(TAG, "mediaPicker selected mediaFile with URL $url")
                }
            }
        } else {
            VASTLog.w(TAG, "mediaPicker: We don't have a compatible media file to play.")
        }
        VASTLog.d(TAG, "Validator returns: " + if (isValid) "valid" else "not valid (no media file)")
        return isValid
    }

    private fun validateModel(model: VASTModel?): Boolean {
        VASTLog.d(TAG, "validateModel")
        var isValid = true

        // There should be at least one impression.
        val impressions = model?.impressions
        if (impressions == null || impressions.isEmpty()) {
            isValid = false
        }

        // There must be at least one VASTMediaFile object
        val mediaFiles = model?.mediaFiles
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            VASTLog.d(TAG, "Validator error: mediaFile list invalid")
            isValid = false
        }
        return isValid
    }
}