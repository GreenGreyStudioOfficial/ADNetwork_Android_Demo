package com.mobileadvsdk.presentation

import android.Manifest
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdNetworkSDK
import com.mobileadvsdk.R
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class PermissionsActivity : AppCompatActivity() {

    private val viewModel: AdvViewModel? = AdNetworkSDK.provider

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent)
        locationTask()
    }

    private fun hasLocationPermissions(): Boolean =
        EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

    @AfterPermissionGranted(RC_LOCATION_PERM)
    fun locationTask() {
        if (hasLocationPermissions()) {
           viewModel?.getLocation()
            finish()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Location",
                RC_LOCATION_PERM,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}

private const val RC_LOCATION_PERM = 999