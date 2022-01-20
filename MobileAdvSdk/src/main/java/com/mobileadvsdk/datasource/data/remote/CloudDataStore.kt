package com.mobileadvsdk.datasource.data.remote

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Completable
import io.reactivex.Single

internal interface CloudDataStore {

    fun loadStartData(advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote>

    fun getNurl(url: String): Completable

    fun getLurl(string: String): Completable
}