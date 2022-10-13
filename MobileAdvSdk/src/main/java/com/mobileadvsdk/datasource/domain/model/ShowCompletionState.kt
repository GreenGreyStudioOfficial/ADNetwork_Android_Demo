package com.mobileadvsdk.datasource.domain.model

import androidx.annotation.Keep

@Keep
enum class ShowCompletionState {
    START,
    CLOSE,
    OFFER,
    SKIP,
    FIRST_QUARTILE,
    MIDPOINT,
    THIRD_QUARTILE,
    COMPLETE
}