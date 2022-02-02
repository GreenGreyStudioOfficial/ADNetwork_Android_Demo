package net.pubnative.player.processor

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlin.Throws
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import net.pubnative.player.processor.CacheFileManager
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.*
import java.io.IOException

class CacheFileManager {
    private val CACHE_SIZE = 90 * 1024 * 1024L
    private var cacheWriter: CacheWriter? = null
    @Throws(IOException::class)
    fun cache(context: Context, uri: Uri?) {
        if (cacheWriter == null) {
            cacheWriter = CacheWriter(
                CacheDataSource.Factory()
                    .setCache(getSimpleCache(context)!!)
                    .setUpstreamDataSourceFactory(
                        DefaultHttpDataSource.Factory()
                            .setAllowCrossProtocolRedirects(true)
                    )
                    .createDataSource(),
                DataSpec(uri!!),
                true,
                null
            ) { requestLength: Long, bytesCached: Long, newBytesCached: Long ->
                val downloadPercentage = (bytesCached * 100.0
                        / requestLength)
                Log.d("CacheFileManager", "downloadPercentage = $downloadPercentage")
            }
        }
        cacheWriter!!.cache()
    }

    fun getSimpleCache(context: Context): Cache? {
        if (simpleCache == null) {
            simpleCache = SimpleCache(
                context.cacheDir,
                LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                ExoDatabaseProvider(context)
            )
        }
        return simpleCache
    }

    fun clearCache(context: Context) {
        getSimpleCache(context)!!.release()
        simpleCache = null
        cacheWriter = null
    }

    companion object {
        private var cacheFileManager: CacheFileManager? = null
        private var simpleCache: Cache? = null
        @JvmStatic
        val instance: CacheFileManager?
            get() {
                if (cacheFileManager == null) {
                    cacheFileManager = CacheFileManager()
                }
                return cacheFileManager
            }
    }
}