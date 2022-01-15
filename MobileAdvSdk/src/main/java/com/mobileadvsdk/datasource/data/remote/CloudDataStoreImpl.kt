package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.api.DataApiService
import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Single

internal class CloudDataStoreImpl(private val serviceApi: DataApiService) : CloudDataStore {

    override fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote> =
        serviceApi.loadStartData(advDataRequestRemote)
}