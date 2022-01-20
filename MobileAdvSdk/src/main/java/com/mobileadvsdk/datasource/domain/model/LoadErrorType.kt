package com.mobileadvsdk.datasource.domain.model

enum class LoadErrorType {
    UNKNOWN,
    CONNECTION_ERROR,
    DATA_PROCESSING_ERROR,
    PROTOCOL_ERROR,
    NOT_INITIALIZED_ERROR,
    TO_MANY_VIDEOS_LOADED,
    AVAILABLE_VIDEO_NOT_FOUND,
    NO_CONTENT
}