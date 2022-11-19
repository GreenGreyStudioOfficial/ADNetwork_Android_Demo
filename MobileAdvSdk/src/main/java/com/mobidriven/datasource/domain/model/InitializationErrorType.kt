package com.mobidriven.datasource.domain.model

import androidx.annotation.Keep

@Keep
enum class InitializationErrorType {
    SDK_ALREADY_INITIALIZED,
    GAME_ID_IS_NULL_OR_EMPTY
}
