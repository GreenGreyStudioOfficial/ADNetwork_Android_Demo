package com.mobileadvsdk.presentation.player.util

import android.util.Log

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