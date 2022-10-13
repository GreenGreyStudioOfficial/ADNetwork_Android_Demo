package com.mobileadvsdk.datasource.domain.model

import androidx.annotation.Keep

@Keep
enum class ShowErrorType {
    UNKNOWN,
    ID_NOT_FOUND,
    VIDEO_CACHE_NOT_FOUND,
    VIDEO_DATA_NOT_FOUND,
    NOT_INITIALIZED_ERROR,
    VIDEO_WAS_DELETED
}