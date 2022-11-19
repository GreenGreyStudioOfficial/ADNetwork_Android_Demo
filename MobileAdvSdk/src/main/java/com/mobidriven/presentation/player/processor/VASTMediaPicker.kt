package com.mobidriven.presentation.player.processor

import com.mobidriven.presentation.player.model.VASTMediaFile

internal interface VASTMediaPicker {
    fun pickVideo(list: List<VASTMediaFile?>?): VASTMediaFile?
}