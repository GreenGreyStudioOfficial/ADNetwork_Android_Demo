package com.mobileadvsdk.datasource.remote.api

import io.reactivex.Completable
import retrofit2.http.GET
import retrofit2.http.Url

internal interface EventApiService {

    @GET()
    fun getUrl(@Url url: String): Completable

}