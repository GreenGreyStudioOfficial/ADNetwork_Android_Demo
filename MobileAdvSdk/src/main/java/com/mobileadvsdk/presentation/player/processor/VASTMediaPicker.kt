package com.mobileadvsdk.presentation.player.processor

import com.mobileadvsdk.presentation.player.model.VASTMediaFile

interface VASTMediaPicker {
    fun pickVideo(list: List<VASTMediaFile?>?): VASTMediaFile?
}