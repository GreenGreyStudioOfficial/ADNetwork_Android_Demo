package com.mobileadvsdk.presentation.player.processor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.*
import com.mobileadvsdk.AdvSDK
import com.mobileadvsdk.datasource.domain.model.AdvData
import com.mobileadvsdk.datasource.remote.api.OKHTTP_CONNECT_TIMEOUT_MS
import com.mobileadvsdk.datasource.remote.api.OKHTTP_READ_TIMEOUT_MS
import com.mobileadvsdk.presentation.player.VASTParser
import com.mobileadvsdk.toAdvData
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resumeWithException

private const val CACHE_SIZE = 90 * 1024 * 1024L

internal object CacheFileManager {

    private var cacheWriter: CacheWriter? = null
    private lateinit var simpleCache: Cache

    fun cache(context: Context, uri: Uri) {
        if (cacheWriter == null) {
            cacheWriter = CacheWriter(
                CacheDataSource.Factory()
                    .setCache(getSimpleCache(context))
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

    fun getSimpleCache(context: Context): Cache {
        if (!::simpleCache.isInitialized) {
            simpleCache = SimpleCache(
                context.cacheDir,
                LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                ExoDatabaseProvider(context)
            )
        }
        return simpleCache
    }

    fun clearCache(context: Context = AdvSDK.context) {
        getSimpleCache(context).release()
        simpleCache = SimpleCache(
            context.cacheDir,
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
            ExoDatabaseProvider(context)
        )
        cacheWriter = null
        deleteCache(context)
    }

    private fun deleteCache(context: Context) {
        try {
            val dir = context.cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            Log.e("CacheFileManager", "${e.message}")
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    fun saveAdv(data: AdvData, context: Context = AdvSDK.context) {
        //clear cache before set new
        clearCache()
        val path: File = context.cacheDir
        val file = File(path, "${data.id}.json")
        val stream = FileOutputStream(file)
        stream.use { it.write(data.toJson().toString().toByteArray()) }

        val adm = data.seatbid.firstOrNull()?.bid?.firstOrNull()?.adm
        if (adm?.startsWith("<!DOCTYPE html>") == true) {
            val links = "https[^ \"]+\\.mp4".toRegex().findAll(adm).map { it.value }.toList()
            Log.e("CacheFileManager", "links $links")

            runBlocking(Dispatchers.IO) {
                links.map {
                    launch { downloadVideoAndCache(it) }
                }.joinAll()
            }
        }
        if (adm?.startsWith("<VAST") == true) {
            runBlocking(Dispatchers.IO) {
                try {
                    val processor = VASTProcessor()
                    processor.process(context, adm)
                    processor.model?.pickedMediaFileURL?.let {
                        cache(context, Uri.parse(it))
                    }
                }catch (e:Throwable){
                    Log.e("CacheFileManager", "${e.message}")
                }

            }

        }
    }

    private fun downloadVideoAndCache(url: String, context: Context = AdvSDK.context) {
        val link = URL(url)
        val cache = File(context.cacheDir, url.split("/").last())
//        Log.e("CacheFileManager", "start download $url")
        val urlConnection = (link.openConnection() as HttpURLConnection).apply {
            readTimeout = OKHTTP_READ_TIMEOUT_MS
            connectTimeout = OKHTTP_CONNECT_TIMEOUT_MS
        }

        try {
            urlConnection.inputStream.use { input ->
                cache.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
//            Log.e("CacheFileManager", "download complete")
            urlConnection.disconnect()
        }
    }

    fun loadAdv(advId: String?): AdvData? {
        advId ?: return null
        return try {
            val inputStream = File("${advId}.json").inputStream()
            val json = inputStream.bufferedReader().use { it.readText() }
            json.toAdvData()
        } catch (e: Throwable) {
            null
        }
    }

    fun getVideo(url: String, context: Context = AdvSDK.context): InputStream? {
        return try {
            return File(context.cacheDir, url.split("/").last()).inputStream()
        } catch (e: Throwable) {
            Log.e("CacheFileManager", "${e.message}")
            null
        }
    }
}