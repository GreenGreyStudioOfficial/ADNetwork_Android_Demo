package com.mobileadvsdk.datasource.domain

import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

internal interface DataRepository {

    fun loadStartData(deviceInfo: DeviceInfo): Flow<AdvData>

    fun getUrl(url: String): Flow<Unit>
}