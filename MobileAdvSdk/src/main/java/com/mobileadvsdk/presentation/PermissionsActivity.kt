package com.mobileadvsdk.presentation

import android.Manifest
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.R
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


private const val REQUEST_CHECK_SETTINGS = 789

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent)
        locationTask()
    }

    private fun hasLocationPermissions(): Boolean =
        EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @AfterPermissionGranted(RC_LOCATION_PERM)
    fun locationTask() {
        if (!hasLocationPermissions()) {
            EasyPermissions.requestPermissions(
                this,
                "Location",
                RC_LOCATION_PERM,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
        finish()
    }
}


private const val RC_LOCATION_PERM = 999