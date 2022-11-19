package com.mobidriven.presentation.player

import android.content.Context
import android.net.Uri
import com.mobidriven.presentation.player.model.VASTModel
import com.mobidriven.presentation.player.processor.CacheFileManager
import com.mobidriven.presentation.player.processor.VASTProcessor
import com.mobidriven.presentation.player.util.VASTLog.e
import com.mobidriven.presentation.player.util.VASTLog.v
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

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

    suspend fun parseVast(context: Context, vastText: String) {
        flowOf(vastText)
            .flowOn(Dispatchers.Default)
            .map {
                v(TAG, "doInBackground")
                var result: VASTModel? = null
                resultError = ERROR_NONE
                if (it.isNotEmpty()) {
                    val processor = VASTProcessor()
                    resultError = processor.process(context, it)
                    if (resultError == ERROR_NONE) {
                        processor.model?.let { model ->
                            result = model
                        }
                    }
                }
                if (resultError == ERROR_NONE) {
                    result
                } else {
                    throw Throwable(resultError.toString())
                }
            }
            .flowOn(Dispatchers.Main)
            .catch { error ->
                when (error.message?.toInt()) {
                    ERROR_CACHE -> {
                        listener?.onVASTCacheError(resultError)
                    }
                    else -> {
                        listener?.onVASTParserError(resultError)
                    }
                }
            }
            .collect { result ->
                listener?.onVASTParserFinished(result)
            }
    }
}