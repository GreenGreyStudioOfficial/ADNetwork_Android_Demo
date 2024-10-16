package com.mobidriven.datasource.domain

import com.mobidriven.datasource.domain.model.AdvData
import com.mobidriven.datasource.domain.model.AdvInitData
import com.mobidriven.datasource.domain.model.DeviceInfo
import com.mobidriven.datasource.remote.model.AdvInitDataRemote
import kotlinx.coroutines.flow.Flow

internal interface IDataRepository {

    fun loadStartData(deviceInfo: DeviceInfo, key: String): Flow<AdvData>

    suspend fun sendInitUserData(key: String, data: AdvInitData)

    fun callPixel(url: String)
    fun sendClose(url: String)
}