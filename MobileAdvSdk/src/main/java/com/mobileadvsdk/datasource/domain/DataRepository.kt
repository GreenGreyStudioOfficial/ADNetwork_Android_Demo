package com.mobileadvsdk.datasource.domain

import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.domain.model.DeviceInfo
import io.reactivex.Single

interface DataRepository {

    fun loadStartData(deviceInfo: DeviceInfo): Single<AdvData>
}