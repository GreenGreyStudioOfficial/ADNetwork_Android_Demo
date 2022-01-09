package com.mobileadvsdk.datasource.di

import com.mobileadvsdk.datasource.data.DataRepositoryImpl
import com.mobileadvsdk.datasource.data.remote.CloudDataStore
import com.mobileadvsdk.datasource.data.remote.CloudDataStoreImpl
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.remote.api.DataApiService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit

val dataSourceModule = Kodein.Module("dataSourceModule") {

    bind<DataApiService>() with singleton {
        instance<Retrofit>().create(DataApiService::class.java)
    }

    bind<CloudDataStore>() with singleton { CloudDataStoreImpl(instance()) }



    bind<DataRepository>() with singleton {
        DataRepositoryImpl(
            instance(),
            instance()
        )
    }
}