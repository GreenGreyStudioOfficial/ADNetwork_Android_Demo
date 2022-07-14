package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.api.DataApiService
import com.mobileadvsdk.datasource.remote.api.DataApiServiceImpl
import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import kotlinx.coroutines.flow.Flow

internal class CloudDataStoreImpl(
    private val dataServiceApi: DataApiService
) : CloudDataStore {

    override fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Flow<AdvDataRemote> =
        DataApiServiceImpl.loadStartData(advDataRequestRemote)

    override fun getUrl(url: String): Flow<Unit> = dataServiceApi.getUrl(url)
}