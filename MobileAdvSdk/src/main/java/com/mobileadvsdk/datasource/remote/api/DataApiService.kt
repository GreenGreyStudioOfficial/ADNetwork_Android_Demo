package com.mobileadvsdk.datasource.remote.api

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

internal interface DataApiService {

    @POST("rtb?key=secret")
    fun loadStartData(@Body advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote>

}