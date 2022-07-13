package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

internal interface CloudDataStore {

    fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Flow<AdvDataRemote>

    fun getUrl(url: String): Flow<>
}