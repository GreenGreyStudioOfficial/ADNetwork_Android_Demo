//
// Copyright (c) 2016, PubNative, Nexage Inc.
// All rights reserved.
// Provided under BSD-3 license as follows:
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// Redistributions in binary form must reproduce the above copyright notice, this
// list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// Neither the name of Nexage, PubNative nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package net.pubnative.player

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import net.pubnative.player.VASTParser
import net.pubnative.player.model.VASTModel
import net.pubnative.player.processor.CacheFileManager.Companion.instance
import net.pubnative.player.processor.VASTMediaPicker
import net.pubnative.player.processor.VASTProcessor
import net.pubnative.player.util.DefaultMediaPicker
import net.pubnative.player.util.VASTLog.v
import java.io.IOException

class VASTParser(private val context: Context?) : AsyncTask<String?, Any?, VASTModel?>() {
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

    override fun doInBackground(vararg params: String?): VASTModel? {
        v(TAG, "doInBackground")
        var result: VASTModel? = null
        resultError = ERROR_NONE
        var vastXML: String? = null
        if (params.isNotEmpty()) {
            vastXML = params[0]
        }
        if (vastXML != null) {
            val mediaPicker: VASTMediaPicker = DefaultMediaPicker(context)
            val processor = VASTProcessor(mediaPicker)
            if (params[0]?.let { processor.process(it) } == ERROR_NONE) {
                result = cacheVideoFile(processor.model)
            }
        }
        return result
    }

    private fun cacheVideoFile(model: VASTModel?): VASTModel? {
        try {
            instance!!.cache(context!!, Uri.parse(model!!.pickedMediaFileURL))
            return model
        } catch (e: IOException) {
            resultError = ERROR_CACHE
        }
        return null
    }

    override fun onPostExecute(result: VASTModel?) {
        v(TAG, "onPostExecute")
        if (listener != null) {
            if (result == null) {
                listener!!.onVASTParserError(resultError)
            } else {
                listener!!.onVASTParserFinished(result)
            }
        }
    }

    companion object {
        private val TAG = VASTParser::class.java.name
        const val ERROR_NONE = 0
        const val ERROR_XML_OPEN_OR_READ = 1
        const val ERROR_XML_PARSE = 2
        const val ERROR_POST_VALIDATION = 3
        const val ERROR_EXCEEDED_WRAPPER_LIMIT = 4
        const val ERROR_CACHE = 5
    }
}