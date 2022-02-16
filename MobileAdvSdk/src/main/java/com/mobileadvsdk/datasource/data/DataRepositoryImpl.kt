package com.mobileadvsdk.datasource.data

import com.mobileadvsdk.datasource.data.remote.CloudDataStore
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import com.mobileadvsdk.datasource.toDomain
import com.mobileadvsdk.datasource.toRemote
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

internal class DataRepositoryImpl(
    private val ioScheduler: Scheduler,
    private val cloudDataStore: CloudDataStore
) : DataRepository {

    override fun loadStartData(deviceInfo: DeviceInfo): Single<AdvData> =
        cloudDataStore.loadStartData(deviceInfo.toRemote())
            .map { it.toDomain() }
            .subscribeOn(ioScheduler)


    override fun getUrl(url: String): Completable =
        cloudDataStore.getUrl(url)
            .subscribeOn(ioScheduler)
}