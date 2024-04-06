package com.mobidriven.datasource.data

import android.util.Log
import com.mobidriven.datasource.domain.IDataRepository
import com.mobidriven.datasource.domain.model.AdvData
import com.mobidriven.datasource.domain.model.AdvInitData
import com.mobidriven.datasource.domain.model.DeviceInfo
import com.mobidriven.datasource.remote.api.DataApiServiceImpl
import com.mobidriven.datasource.remote.model.AdvInitDataRemote
import com.mobidriven.toDomain
import com.mobidriven.toRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class DataRepositoryImpl(
    private val cloudDataStore: DataApiServiceImpl = DataApiServiceImpl
) : IDataRepository {

    override fun loadStartData(deviceInfo: DeviceInfo, key: String): Flow<AdvData> {
        Log.e("req", "${deviceInfo.toRemote()} $key")
        return  cloudDataStore.loadStartData(deviceInfo.toRemote(), key)
            .flowOn(Dispatchers.IO)
            .map {
                Log.e("DataRepository", "data $it")
                it.toDomain()
            }
    }


    override suspend fun sendInitUserData(key: String, data: AdvInitData) {
        cloudDataStore.sendInitUserData(key, data.toRemote())
    }


    override fun callPixel(url: String) = cloudDataStore.getUrl(url)
}