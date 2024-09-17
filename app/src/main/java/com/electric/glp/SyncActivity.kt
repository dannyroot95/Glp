package com.electric.glp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SyncActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)
        supportActionBar?.hide()
    }

    override fun onBackPressed() {
    }

}