package com.mobileadvsdk.datasource.remote.api

import com.mobileadvsdk.datasource.remote.model.AdvDataRemote
import com.mobileadvsdk.datasource.remote.model.AdvDataRequestRemote
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

internal interface DataApiService {

    @GET()
    fun getUrl(@Url url: String): Flow<Unit>


    @POST("rtb?key=secret")
    fun loadStartData(@Body advDataRequestRemote: AdvDataRequestRemote): Flow<AdvDataRemote>

}