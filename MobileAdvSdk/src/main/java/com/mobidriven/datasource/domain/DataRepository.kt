package com.mobidriven.datasource.domain

import com.mobidriven.datasource.domain.model.AdvData
import com.mobidriven.datasource.domain.model.DeviceInfo
import com.mobidriven.datasource.remote.model.AdvDataRemote
import kotlinx.coroutines.flow.Flow
import java.nio.channels.spi.AbstractSelectionKey

internal interface DataRepository {

    fun loadStartData(deviceInfo: DeviceInfo, key: String): Flow<AdvData>

    fun callPixel(url: String)
}