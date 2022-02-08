package com.mobileadvsdk.presentation.player.processor

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.*
import com.mobileadvsdk.AdvApplication
import java.io.IOException

private const val CACHE_SIZE = 90 * 1024 * 1024L

internal object CacheFileManager {

    private var cacheWriter: CacheWriter? = null
    private lateinit var simpleCache: Cache

    fun cache(uri: Uri) {
        if (cacheWriter == null) {
            cacheWriter = CacheWriter(
                CacheDataSource.Factory()
                    .setCache(getSimpleCache())
                    .setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true)
                    )
                    .createDataSource(),
                DataSpec(uri),
                true,
                null
            ) { requestLength: Long, bytesCached: Long, _: Long ->
                val downloadPercentage = (bytesCached * 100.0 / requestLength)
                Log.d("CacheFileManager", "downloadPercentage = $downloadPercentage")
            }
        }
        cacheWriter?.cache()
    }

    fun getSimpleCache(): Cache {
        if (!::simpleCache.isInitialized) {
            simpleCache = SimpleCache(
                AdvApplication.instance.cacheDir,
                LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                ExoDatabaseProvider(AdvApplication.instance)
            )
        }
        return simpleCache
    }

    fun clearCache() {
        getSimpleCache().release()
        simpleCache = SimpleCache(
            AdvApplication.instance.cacheDir,
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
            ExoDatabaseProvider(AdvApplication.instance)
        )
        cacheWriter = null
    }
}