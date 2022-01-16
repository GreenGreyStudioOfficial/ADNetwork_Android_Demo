package com.example.advsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.AdvManager
import com.mobileadvsdk.LoadDataListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnStart).setOnClickListener {
            AdvManager.loadData(object : LoadDataListener {
                override fun dataLoadSuccess() {
                   AdvManager.showAdv(this@MainActivity)
                }

                override fun dataLoadFailure() {
                    Log.e("MainActivity", "dataLoadFailure")
                }
            })
        }
    }
}