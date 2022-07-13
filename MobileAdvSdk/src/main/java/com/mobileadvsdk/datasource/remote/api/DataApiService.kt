package com.mobileadvsdk.datasource.remote.api

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.Flow

internal interface DataApiService {

    @GET()
    fun getUrl(@Url url: String): Flow<Unit>


    @POST("rtb?key=secret")
    fun loadStartData(@Body advDataRequestRemote: AdvDataRequestRemote): Flow<AdvDataRemote>

}