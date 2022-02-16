package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.api.DataApiService
import com.mobileadvsdk.datasource.remote.api.EventApiService
import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Completable
import io.reactivex.Single

internal class CloudDataStoreImpl(
    private val dataServiceApi: DataApiService
) : CloudDataStore {

    override fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote> =
        dataServiceApi.loadStartData(advDataRequestRemote)

    override fun getUrl(url: String): Completable = dataServiceApi.getUrl(url)

}