package com.mobileadvsdk.presentation.player

import android.net.Uri
import android.os.AsyncTask
import com.mobileadvsdk.presentation.player.model.VASTModel
import com.mobileadvsdk.presentation.player.processor.CacheFileManager
import com.mobileadvsdk.presentation.player.processor.VASTProcessor
import com.mobileadvsdk.presentation.player.util.VASTLog.e
import com.mobileadvsdk.presentation.player.util.VASTLog.v
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

const val ERROR_NONE = 0
const val ERROR_XML_OPEN_OR_READ = 1
const val ERROR_XML_PARSE = 2
const val ERROR_POST_VALIDATION = 3
const val ERROR_EXCEEDED_WRAPPER_LIMIT = 4
const val ERROR_CACHE = 5

private val TAG = VASTParser::class.java.name

internal object VASTParser {

    private var listener: Listener? = null
    private var resultError = ERROR_NONE

    interface Listener {
        fun onVASTParserError(error: Int)
        fun onVASTCacheError(error: Int)
        fun onVASTParserFinished(model: VASTModel?)
    }

    fun setListener(listener: Listener?): VASTParser {
        v(TAG, "setListener")
        this.listener = listener
        return this
    }

    private fun cacheVideoFile(url: String): Int {
        try {
            CacheFileManager.cache(Uri.parse(url))
            return ERROR_NONE
        } catch (e: Throwable) {
            e(TAG, "cacheVideoFile", e)
        }
        return ERROR_CACHE
    }

    fun parseVast(vastText: String) =
        Single.just(vastText)
            .subscribeOn(Schedulers.computation())
            .map {
                v(TAG, "doInBackground")
                var result: VASTModel? = null
                resultError = ERROR_NONE
                if (it.isNotEmpty()) {
                    val processor = VASTProcessor()
                    resultError = processor.process(it)
                    if (resultError == ERROR_NONE) {
                        processor.model?.let { model ->
                            resultError = cacheVideoFile(model.pickedMediaFileURL)
                            if (resultError == ERROR_NONE) {
                                result = model
                            }
                        }
                    }
                }
                if (resultError == ERROR_NONE) {
                    result
                } else {
                    throw Throwable(resultError.toString())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    listener?.onVASTParserFinished(result)
                },
                { error ->
                    when (error.message?.toInt()) {
                        ERROR_CACHE -> {
                            listener?.onVASTCacheError(resultError)
                        }
                        else -> {
                            listener?.onVASTParserError(resultError)
                        }
                    }
                }
            )
}