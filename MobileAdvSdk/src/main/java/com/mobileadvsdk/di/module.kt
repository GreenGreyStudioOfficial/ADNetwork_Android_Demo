package com.mobileadvsdk.di


import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mobileadvsdk.datasource.data.DataRepositoryImpl
import com.mobileadvsdk.datasource.data.remote.CloudDataStore
import com.mobileadvsdk.datasource.data.remote.CloudDataStoreImpl
import com.mobileadvsdk.datasource.domain.DataRepository
import com.mobileadvsdk.datasource.remote.api.DataApiService
import com.mobileadvsdk.datasource.remote.api.EventApiService
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal fun mainModule(host: String) = Kodein.Module("main") {

    bind<DataApiService>() with singleton {
        instance<Retrofit>("dataService").create(DataApiService::class.java)
    }

    bind<EventApiService>() with singleton {
        instance<Retrofit>("eventService").create(EventApiService::class.java)
    }

    bind<CloudDataStore>() with singleton { CloudDataStoreImpl(instance(), instance()) }

    bind<DataRepository>() with singleton {
        DataRepositoryImpl(
            instance("ioScheduler"),
            instance()
        )
    }

    bind<Gson>() with singleton {
        GsonBuilder()
            .create()

    }

    bind<OkHttpClient>() with singleton {
        OkHttpClient.Builder()
            .connectTimeout(OKHTTP_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(OKHTTP_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build()
    }

    bind<GsonConverterFactory>() with singleton {
        GsonConverterFactory.create(instance())
    }

    bind<RxJava2CallAdapterFactory>() with singleton {
        RxJava2CallAdapterFactory.create()
    }

    bind<Retrofit>("dataService") with singleton {
        Retrofit.Builder()
            .client(instance())
            .baseUrl(host)
            .addConverterFactory(instance())
            .addCallAdapterFactory(instance())
            .build()
    }

    bind<Retrofit>("eventService") with singleton {
        Retrofit.Builder()
            .client(instance())
            .baseUrl(host)
            .addConverterFactory(instance())
            .addCallAdapterFactory(instance())
            .build()
    }

    bind<Scheduler>("uiScheduler") with singleton {
        AndroidSchedulers.mainThread()
    }

    bind<Scheduler>("computationScheduler") with singleton {
        Schedulers.computation()
    }

    bind<Scheduler>("ioScheduler") with singleton {
        Schedulers.io()
    }

    bind<SharedPreferences>() with singleton {
        kodein.direct.instance<Context>()
            .getSharedPreferences(PREF_CONTAINER_NAME, Context.MODE_PRIVATE)
    }

}

private const val OKHTTP_CONNECT_TIMEOUT_MS = 20_000L
private const val OKHTTP_READ_TIMEOUT_MS = 20_000L
private const val PREF_CONTAINER_NAME = "prefs"