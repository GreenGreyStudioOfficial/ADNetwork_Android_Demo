package com.example.advsdk

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdvController
import com.mobileadvsdk.AdvControllerImpl
import com.mobileadvsdk.AdvManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnStart).setOnClickListener {
//            startActivity(Intent(this, AdvActivity::class.java))
            AdvManager( ).loadData()

        }
    }
}