package com.mobileadvsdk.datasource.data

import android.util.Log
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import com.mobileadvsdk.datasource.remote.api.DataApiServiceImpl
import com.mobileadvsdk.toDomain
import com.mobileadvsdk.toRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class DataRepositoryImpl(
    private val cloudDataStore: DataApiServiceImpl = DataApiServiceImpl
) : DataRepository {

    override fun loadStartData(deviceInfo: DeviceInfo, key: String): Flow<AdvData> =
        cloudDataStore.loadStartData(deviceInfo.toRemote(), key)
            .flowOn(Dispatchers.IO)
            .map {
//                Log.e("DataRepositoryImpl", "data $it")
                it.toDomain()
            }


    override fun callPixel(url: String) = cloudDataStore.getUrl(url)
}