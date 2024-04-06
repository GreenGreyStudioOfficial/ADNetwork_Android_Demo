package com.mobidriven.datasource.domain.model

import androidx.annotation.Keep

@Keep
enum class ShowErrorType {
    UNKNOWN,
    ID_NOT_FOUND,
    ADV_CACHE_NOT_FOUND,
    VIDEO_DATA_NOT_FOUND,
    NOT_INITIALIZED_ERROR,
    ACTIVITY_WAS_DESTROYED,
    BANNER_VIEW_NOT_FOUND,
}