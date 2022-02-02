//
//  NetworkTools.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//
package net.pubnative.player.util

import android.content.Context
import net.pubnative.player.util.VASTLog
import net.pubnative.player.util.NetworkTools
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkTools {
    private val TAG = NetworkTools::class.java.name

    // This method return true if it's connected to Internet
    fun isConnectedToInternet(context: Context): Boolean {
        VASTLog.d(TAG, "Testing connectivity:")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiNetwork != null && wifiNetwork.isConnected) {
            VASTLog.d(TAG, "Connected to Internet")
            return true
        }
        val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mobileNetwork != null && mobileNetwork.isConnected) {
            VASTLog.d(TAG, "Connected to Internet")
            return true
        }
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected) {
            VASTLog.d(TAG, "Connected to Internet")
            return true
        }
        VASTLog.d(TAG, "No Internet connection")
        return false
    }
}