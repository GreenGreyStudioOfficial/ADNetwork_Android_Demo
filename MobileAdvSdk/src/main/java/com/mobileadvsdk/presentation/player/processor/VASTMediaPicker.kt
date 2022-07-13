package com.mobileadvsdk.presentation.player.processor

import com.mobileadvsdk.presentation.player.model.VASTMediaFile

internal interface VASTMediaPicker {
    fun pickVideo(list: List<VASTMediaFile?>?): VASTMediaFile?
}