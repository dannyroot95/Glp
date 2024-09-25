package com.electric.glp.Activitys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.electric.glp.R
import com.electric.glp.Adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.electric.glp.databinding.ActivityMenuBinding
import com.google.firebase.auth.FirebaseAuth

class ActivityMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        binding.btnLogout.setOnClickListener {
            // Crear un AlertDialog Builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cerrar Sesión")
            builder.setIcon(R.drawable.ic_alert)
            builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")

            // Agregar botón de confirmación
            builder.setPositiveButton("Sí") { dialog, which ->
                auth.signOut()
                clearSpecificPreferences()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            // Agregar botón de cancelación
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            // Mostrar el AlertDialog
            builder.show()
        }


        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            // Inflar el layout personalizado
            val tabView = LayoutInflater.from(this).inflate(R.layout.tab_custom_view, null)
            val tabIcon = tabView.findViewById<ImageView>(R.id.tab_icon)
            val tabText = tabView.findViewById<TextView>(R.id.tab_text)

            tabText.text = when (position) {
                0 -> "Monitoreo"
                1 -> "Historial"
                2 -> "Configurar"
                else -> null
            }
            tabIcon.setImageDrawable(when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.ic_monitoring)
                1 -> ContextCompat.getDrawable(this, R.drawable.ic_history)
                2 -> ContextCompat.getDrawable(this, R.drawable.ic_settings)
                else -> null
            })

            // Aplicar el view personalizado al tab
            tab.customView = tabView
        }.attach()
        binding.tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.green))
    }

    private fun clearSpecificPreferences() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove("userId")
            remove("deviceId")
            apply()
        }
    }

    override fun onBackPressed() {
        // Prevent back press
    }
}
