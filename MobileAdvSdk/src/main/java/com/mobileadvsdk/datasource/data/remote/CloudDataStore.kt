package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Single

interface CloudDataStore {

    fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote>
}