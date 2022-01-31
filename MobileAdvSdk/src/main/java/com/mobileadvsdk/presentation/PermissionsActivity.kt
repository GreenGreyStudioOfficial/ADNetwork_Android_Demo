package com.mobileadvsdk.presentation

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.mobileadvsdk.R
import kotlinx.android.synthetic.main.activity_adv.*
import kotlinx.android.synthetic.main.dialog_close_advert.view.*

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent)
    }

}