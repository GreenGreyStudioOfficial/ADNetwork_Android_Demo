package com.mobileadvsdk.datasource.remote.api

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface EventApiService {

    @POST("rtb?key=secret")
    fun loadStartData(@Body advDataRequestRemote: AdvDataRequestRemote): Single<AdvDataRemote>

    @GET("{url}")
    fun getNurl(@Path("url") url: String): Completable

}