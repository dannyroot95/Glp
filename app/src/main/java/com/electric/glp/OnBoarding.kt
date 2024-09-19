package com.electric.glp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.electric.glp.databinding.ActivityOnboardingBinding

class OnBoarding : AppCompatActivity() {

    private lateinit var binding : ActivityOnboardingBinding
    private lateinit var indicators: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = MyPagerAdapter(this)
        setupIndicators(3)  // Asumiendo que MyPagerAdapter tiene una constante con el número de páginas
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })
    }



    private fun setupIndicators(count: Int) {
        indicators = Array(count) { ImageView(this) }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i].apply {
                this.layoutParams = layoutParams
                this.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.indicator_inactive))
                binding.indicatorContainer.addView(this)
            }
        }

        updateIndicators(0)  // Default to first page
    }

    private fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            if (i == position) {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active))
                Log.d("Onboarding", "Active Indicator: $i")
            } else {
                indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive))
                Log.d("Onboarding", "Inactive Indicator: $i")
            }
        }
    }

    private fun getUserDetailsFromPreferences(): Pair<String?, String?> {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "")   // Retorna null si no existe la clave "userId"
        val deviceId = prefs.getString("deviceId", "") // Retorna null si no existe la clave "deviceId"
        return Pair(userId, deviceId)
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        if (onboardingCompleted) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            // Si el onboarding está completo, inicia directamente el siguiente Activity
        }
    }

}
