package com.electric.glp.Activitys

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.electric.glp.R
import com.electric.glp.Adapters.ViewPagerAdapter
import com.electric.glp.Services.GLPMonitoringService
import com.google.android.material.tabs.TabLayoutMediator
import com.electric.glp.databinding.ActivityMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class ActivityMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        requestNotificationPermission()

        auth = FirebaseAuth.getInstance()

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userId = prefs.getString("userId", null) ?: ""

        if (userId.isNotEmpty()) {
            checkNotificationStatusAndManageService()
        }

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
                stopService(Intent(this, GLPMonitoringService::class.java))
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

    private fun checkNotificationStatusAndManageService() {
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(userId)

        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val notificationStatus = document.getBoolean("notificationStatus") ?: false
                manageService(notificationStatus)
            } else {
            }
        }.addOnFailureListener { exception ->
        }
    }

    private fun manageService(notificationStatus: Boolean) {
        if (notificationStatus && !isMyServiceRunning(GLPMonitoringService::class.java)) {
            // Iniciar el servicio si no está corriendo
            val intent = Intent(this, GLPMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else if (!notificationStatus && isMyServiceRunning(GLPMonitoringService::class.java)) {
            // Detener el servicio si está corriendo
            val intent = Intent(this, GLPMonitoringService::class.java)
            stopService(intent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { it.service.className == serviceClass.name }
    }

    private fun clearSpecificPreferences() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            remove("userId")
            remove("deviceId")
            apply()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 y superior
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido
            } else {
                requestNotificationPermission()            }
        }
    }

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onBackPressed() {
        // Prevent back press
    }
}
