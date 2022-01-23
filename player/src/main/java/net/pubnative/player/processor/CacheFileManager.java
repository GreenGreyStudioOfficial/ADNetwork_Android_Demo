package net.pubnative.player.processor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheWriter;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.IOException;

public class CacheFileManager {

    private static CacheFileManager cacheFileManager;
    private static Cache simpleCache;
    private final long CACHE_SIZE = 90 * 1024 * 1024L;

    public static CacheFileManager getInstance() {
        if (cacheFileManager == null) {
            cacheFileManager = new CacheFileManager();
        }
        return cacheFileManager;
    }

    private CacheWriter cacheWriter;

    public void cache(Context context, Uri uri) throws IOException {
        if (cacheWriter == null) {
            cacheWriter = new CacheWriter(
                    new CacheDataSource.Factory()
                            .setCache(getSimpleCache(context))
                            .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                    .setAllowCrossProtocolRedirects(true))
                            .createDataSource(),
                    new DataSpec(uri),
                    true,
                    null,
                    (requestLength, bytesCached, newBytesCached) -> {
                        double downloadPercentage = (bytesCached * 100.0
                                / requestLength);
                        Log.d("CacheFileManager", "downloadPercentage = " + downloadPercentage);
                    }
            );
        }
        cacheWriter.cache();
    }

    public Cache getSimpleCache(Context context) {
        if (simpleCache == null) {
            simpleCache = new SimpleCache(context.getCacheDir(),
                    new LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                    new ExoDatabaseProvider(context));
        }
        return simpleCache;
    }

}
