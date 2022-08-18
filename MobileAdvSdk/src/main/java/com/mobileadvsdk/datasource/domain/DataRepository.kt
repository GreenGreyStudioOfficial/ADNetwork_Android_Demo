package com.mobileadvsdk.datasource.domain

import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import kotlinx.coroutines.flow.Flow
import java.nio.channels.spi.AbstractSelectionKey

internal interface DataRepository {

    fun loadStartData(deviceInfo: DeviceInfo, key: String): Flow<AdvData>

    fun callPixel(url: String)
}