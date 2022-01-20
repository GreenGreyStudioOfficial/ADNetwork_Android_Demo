package com.mobileadvsdk.datasource.domain.model

enum class LoadErrorType ( val desc:String){
    UNKNOWN("Неизвестная ошибка"),
    CONNECTION_ERROR("Ошибка соединения"),
    DATA_PROCESSING_ERROR("Ошибка обработки данных"),
    PROTOCOL_ERROR("Ошибка протокола"),
    NOT_INITIALIZED_ERROR("Отсутствует инициализация"),
    TO_MANY_VIDEOS_LOADED("Уже загруженно слишком много видео данного типа"),
    AVAILABLE_VIDEO_NOT_FOUND(" Сервис предоставления рекламы не нашел соответствующий ролик"),
    NO_CONTENT("Нет контента");
}