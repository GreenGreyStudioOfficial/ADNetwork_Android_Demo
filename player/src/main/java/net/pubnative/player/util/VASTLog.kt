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
package net.pubnative.player.util

import android.util.Log
import net.pubnative.player.util.VASTLog.LOG_LEVEL
import net.pubnative.player.util.VASTLog

object VASTLog {
    private val TAG = VASTLog::class.java.name
    private var LEVEL = LOG_LEVEL.verbose
    @JvmStatic
	fun v(tag: String?, msg: String?) {
        if (LEVEL.value <= LOG_LEVEL.verbose.value) {
            Log.v(tag, msg!!)
        }
    }

    @JvmStatic
	fun d(tag: String?, msg: String?) {
        if (LEVEL.value <= LOG_LEVEL.debug.value) {
            Log.d(tag, msg!!)
        }
    }

    @JvmStatic
	fun i(tag: String?, msg: String?) {
        if (LEVEL.value <= LOG_LEVEL.info.value) {
            Log.i(tag, msg!!)
        }
    }

    @JvmStatic
	fun w(tag: String?, msg: String?) {
        if (LEVEL.value <= LOG_LEVEL.warning.value) {
            Log.w(tag, msg!!)
        }
    }

    fun e(tag: String?, msg: String?) {
        if (LEVEL.value <= LOG_LEVEL.error.value) {
            Log.e(tag, msg!!)
        }
    }

    @JvmStatic
	fun e(tag: String?, msg: String?, tr: Throwable?) {
        if (LEVEL.value <= LOG_LEVEL.error.value) {
            Log.e(tag, msg, tr)
        }
    }

    var loggingLevel: LOG_LEVEL
        get() = LEVEL
        set(logLevel) {
            Log.i(TAG, "Changing logging level from :$LEVEL. To:$logLevel")
            LEVEL = logLevel
        }

    enum class LOG_LEVEL(val value: Int) {
        verbose(1), debug(2), info(3), warning(4), error(5), none(6);

    }
}