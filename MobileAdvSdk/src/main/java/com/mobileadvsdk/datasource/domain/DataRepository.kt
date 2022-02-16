package com.mobileadvsdk.datasource.domain

import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import io.reactivex.Completable
import io.reactivex.Single

internal interface DataRepository {

    fun loadStartData(deviceInfo: DeviceInfo): Single<AdvData>

    fun getUrl(url: String): Completable
}