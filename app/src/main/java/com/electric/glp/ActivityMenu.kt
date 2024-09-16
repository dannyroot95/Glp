package com.electric.glp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.electric.glp.databinding.ActivityMainBinding
import com.electric.glp.databinding.ActivityMenuBinding

class ActivityMenu : AppCompatActivity() {

    private lateinit var binding : ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
    }
}