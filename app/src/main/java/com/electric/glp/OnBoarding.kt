package com.electric.glp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.electric.glp.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnBoarding : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        viewPager.adapter = MyPagerAdapter(this)
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        if (onboardingCompleted) {
            // Si el onboarding está completo, inicia directamente el siguiente Activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
