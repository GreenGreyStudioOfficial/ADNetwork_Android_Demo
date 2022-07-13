package com.mobileadvsdk.datasource.data

import com.mobileadvsdk.datasource.data.remote.CloudDataStore
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import com.mobileadvsdk.datasource.toDomain
import com.mobileadvsdk.datasource.toRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

internal class DataRepositoryImpl(
    private val cloudDataStore: CloudDataStore
) : DataRepository {

    override fun loadStartData(deviceInfo: DeviceInfo): Flow<AdvData> =
        cloudDataStore.loadStartData(deviceInfo.toRemote())
            .map { it.toDomain() }
            .flowOn(Dispatchers.IO)

    override fun getUrl(url: String): Flow<Unit> =
        cloudDataStore.getUrl(url)
            .flowOn(Dispatchers.IO)
}